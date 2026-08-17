# 이미지 처리 병렬화와 DB–S3 정합성

게시글 원본 이미지를 그대로 저장해 전송량과 저장 공간이 커지던 문제를 두 가지 출력 규격으로 줄이고, 이미지 처리를 Lambda로 분리한 뒤에도 동기 완료 계약과 DB–S3 정합성을 지킨 사례입니다. 순차 호출, 고정 Platform Thread 풀, Virtual Thread와 세마포어를 비교해 외부 호출의 실행 모델과 동시성 제한을 분리했습니다.

전체 Cluverse 서버에서 이미지 업로드 orchestration, 외부 저장소·프로세서 경계, 실행 모델별 Processor, 예약·보상·조정 계약과 핵심 테스트만 선별하고 기존 사례의 패키지·의존 방향으로 리팩토링했습니다.

## 관련 글

- [Cluverse #11: 이미지 처리 완료와 DB–S3 정합성](https://velog.io/@bebeis/cluverse-11-image-optimize)

## 문제

- 원본 이미지를 그대로 제공하면 해상도와 메타데이터가 불필요하게 남아 네트워크 전송량과 저장 비용이 커집니다.
- 여러 이미지의 CPU 작업을 Spring 서버가 수행하면 API 처리와 자원 경쟁이 발생합니다.
- Lambda를 이미지마다 순차 동기 호출하면 이미지 수만큼 외부 대기 시간이 누적됩니다.
- DB 트랜잭션과 S3 객체 저장은 하나의 원자적 커밋으로 묶을 수 없어 부분 실패 시 고아 객체나 존재하지 않는 객체를 가리키는 레코드가 남을 수 있습니다.
- Lambda timeout 뒤 즉시 객체를 삭제하면 아직 실행 중인 Lambda의 늦은 쓰기와 보상이 경합할 수 있습니다.

## 해결

1. 원본은 staging key에 두고, Lambda가 본문용 이미지와 썸네일을 재인코딩해 메타데이터를 제거합니다.
2. 서버는 각 이미지의 staging/output key와 처리 정책 버전을 담은 불변 명령만 Lambda에 전달합니다.
3. `requestId`와 예정 object key를 짧은 DB 트랜잭션에서 `PENDING`으로 예약합니다.
4. DB 트랜잭션 밖에서 S3 업로드와 Lambda 처리를 실행하고 실제 출력 객체의 존재와 크기를 확인합니다.
5. 모든 이미지가 성공한 경우에만 새 트랜잭션으로 결과를 `COMPLETED`로 확정하고 staging 객체를 정리합니다.
6. 일반 실패는 `PENDING → COMPENSATING`을 원자적으로 선점한 작업만 객체를 삭제하고 `FAILED`로 바꿉니다.
7. timeout은 최대 Lambda 실행 시간이 지난 stale `PENDING`을 조정 작업이 선점해 보상합니다.
8. `(version, requestId)` unique 제약을 최종 멱등성 경계로 두고, 완료된 재요청은 기존 결과를 반환합니다.

## 실패 결과

| 실패 지점 | 남는 결과와 후속 처리 |
| --- | --- |
| 예약 전 실패 | DB·S3 결과 없음 |
| staging 업로드 또는 Lambda 처리 실패 | 보상 선점 후 관련 객체 삭제, `FAILED` |
| Lambda timeout | `PENDING` 유지, 최대 실행 시간 뒤 조정 작업이 보상 |
| 출력 객체 검증 실패 | DB 확정 전 중단하고 관련 객체 보상 |
| DB 완료 트랜잭션 실패 | 출력 객체 보상, `FAILED` |
| 완료 후 staging 삭제 실패 | `COMPLETED` 유지, 정리 작업 재시도 |
| 동일 `requestId` 재요청 | `COMPLETED`는 기존 결과 반환, 진행·실패 상태는 중복 실행 차단 |

## 버전별 코드

- [`v1-sequential-lambda-processing/`](./v1-sequential-lambda-processing/): 요청 스레드에서 이미지별 Lambda 순차 호출
- [`v2-platform-thread-parallel-processing/`](./v2-platform-thread-parallel-processing/): 고정 Platform Thread 풀과 `CompletableFuture` 병렬 호출
- [`v3-virtual-thread-bounded-processing/`](./v3-virtual-thread-bounded-processing/): Virtual Thread와 세마포어를 분리한 제한 병렬 호출

세 버전은 같은 `PostImageUploadService` 진입점과 예약·완료·보상 프로토콜을 유지합니다. Service는 Repository를 직접 참조하지 않고 짧은 트랜잭션을 소유하는 Writer만 호출하며, S3와 Lambda SDK는 client 경계 밖으로 노출하지 않습니다.

실제 S3·Lambda 구현, multipart 임시 파일 생성, JPA 엔티티와 unique DDL, Nginx·Spring 용량 제한, 메트릭과 부하 스크립트는 생략해 독립 빌드를 보장하지 않습니다.

## 결과

### 제어된 외부 호출 스텁, 동시 요청 64

| 버전 | p95 | p99 | 처리량 | Platform Thread 수 |
| --- | ---: | ---: | ---: | ---: |
| V1 순차 호출 | 5,307.9ms | 8,120.7ms | 16.09 req/s | 75 |
| V2 고정 풀 | 2,850.0ms | 3,581.1ms | 32.71 req/s | 151 |
| V3 Virtual Thread + Semaphore | 2,763.3ms | 3,358.1ms | 33.24 req/s | 87 |

V3는 V2와 처리량이 비슷하면서 Platform Thread를 약 42% 적게 사용했고 p99를 약 6% 낮췄습니다. 실제 AWS Lambda 환경에서도 V3는 V1 대비 처리량 25% 증가·p99 8% 감소, V2 대비 처리량 8% 증가·p99 10% 감소를 보였습니다. failure injection에서는 HTTP 500 요청이 `FAILED`와 객체 정리로 수렴했고, 누락 객체를 가리키는 `COMPLETED`, 고아 S3 객체와 stale `PENDING`이 남지 않는지 함께 확인했습니다.
