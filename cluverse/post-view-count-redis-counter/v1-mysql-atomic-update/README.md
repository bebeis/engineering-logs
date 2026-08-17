# v1 - MySQL Atomic Update

게시글 조회마다 MySQL이 `view_count = view_count + 1`을 원자적으로 실행하는 비교 기준선입니다.

## 요청 흐름

```text
PostViewCountCommandService.increaseViewCount
  -> PostMetaWriter.increaseViewCount
    -> PostViewCountRepository.increaseCount
  -> PostMetaReader.readViewCount
```

## 적용한 리팩토링

- Devlog 4의 원자적 UPDATE 경계를 유지하고 Devlog 5의 공통 결과 타입을 추가했습니다.
- 조회수 쓰기와 읽기를 `PostMetaWriter`, `PostMetaReader`로 분리했습니다.
- Controller와 게시글 존재 검증은 모든 버전에 공통이므로 제외했습니다.

## 핵심 코드

1. [PostViewCountCommandService.java](./src/main/java/cluverse/post/service/PostViewCountCommandService.java)
2. [PostMetaWriter.java](./src/main/java/cluverse/meta/service/implement/PostMetaWriter.java)
3. [PostMetaReader.java](./src/main/java/cluverse/meta/service/implement/PostMetaReader.java)
4. [PostViewCountRepository.java](./src/main/java/cluverse/meta/repository/PostViewCountRepository.java)
5. [PostViewCountCommandServiceTest.java](./src/test/java/cluverse/post/service/PostViewCountCommandServiceTest.java)

## 검증과 한계

- 갱신 유실 없이 단순하지만 단일 핫 레코드는 약 280 TPS부터 락 대기가 증가합니다.
- 핵심 계약 테스트는 포함하지만 공통 빌드 설정이 없어 이 저장소에서는 실행하지 않습니다.
