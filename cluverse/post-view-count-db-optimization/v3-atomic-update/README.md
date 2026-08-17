# v3 - Atomic Update

조회수 레코드를 먼저 읽지 않고 DB가 `view_count = view_count + 1`을 원자적으로 실행하도록 바꾼 버전입니다.

## 이전 버전과의 차이

- `SELECT FOR UPDATE`와 엔티티 변경 감지를 제거합니다.
- 락을 쥔 구간에서 DB와 애플리케이션 사이의 왕복을 제거합니다.
- 영향받은 행이 정확히 한 건인지 확인해 존재하지 않는 조회수 레코드를 구분합니다.

## 요청 흐름

```text
PostViewCountCommandService.increaseViewCount  # 트랜잭션 시작
  -> PostMetaWriter.increaseViewCount
    -> PostViewCountRepository.increaseCount  # UPDATE view_count = view_count + 1
  -> COMMIT 후 DB 레코드 락 해제
```

## 적용한 리팩토링

- Service의 공개 계약을 앞 버전과 동일하게 유지합니다.
- 원자적 갱신은 `cluverse.meta.service.implement.PostMetaWriter`가 맡습니다.
- 원자적 UPDATE는 `cluverse.meta.repository.PostViewCountRepository`에 보존합니다.
- 전체 `PostMetaWriter`와 Repository에서 원자적 UPDATE 비교에 필요한 코드만 남깁니다.
- 테스트는 Writer가 Repository의 원자적 증가를 호출하는 계약을 검증합니다.

## 핵심 코드

1. [PostViewCountCommandService.java](./src/main/java/cluverse/post/service/PostViewCountCommandService.java)
   - 버전 간 동일한 조회수 증가 유스케이스
2. [PostMetaWriter.java](./src/main/java/cluverse/meta/service/implement/PostMetaWriter.java)
   - 쓰기 흐름과 트랜잭션 경계 소유
3. [PostViewCountRepository.java](./src/main/java/cluverse/meta/repository/PostViewCountRepository.java)
   - 원자적 네이티브 UPDATE
4. [PostMetaWriterTest.java](./src/test/java/cluverse/meta/service/implement/PostMetaWriterTest.java)
   - 원자적 증가 위임 계약
5. [post-view-count.sql](./src/main/resources/post-view-count.sql)
   - 게시글과 분리된 조회수 테이블
6. [PostViewCountCommandServiceTest.java](./src/test/java/cluverse/post/service/PostViewCountCommandServiceTest.java)
   - 원자적 UPDATE 전략 위임 계약

## 검증과 남은 한계

- 100 RPS에서 평균 응답 시간이 비관적 락의 127ms에서 51ms로 감소했습니다.
- DB 직접 실험에서 같은 레코드는 1커넥션 285 TPS, 20커넥션 288 TPS로 커넥션 수와 무관하게 비슷했습니다.
- 서로 다른 레코드에 쓰기를 분산하면 그룹 커밋으로 3,128 TPS를 기록했습니다.
- 단일 레코드에서는 커밋마다 약 3.5ms의 fsync가 필요해 약 285 TPS가 RDB 카운터의 사실상 상한이었습니다.
- Redis write-back은 조회 N건을 한 번의 DB 반영으로 합칠 수 있지만 이 글에서는 설계만 했으므로 구현하지 않습니다.
- 핵심 계약 테스트는 포함하지만 전체 빌드와 DB 프로시저, k6 시나리오는 없어 이 저장소에서는 실행하지 않습니다.
