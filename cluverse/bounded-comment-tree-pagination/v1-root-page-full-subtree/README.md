# v1 - Root Page with Full Subtrees

루트 댓글을 Offset으로 페이지한 뒤 선택된 각 루트 아래의 모든 자손을 최대 depth 5까지 한 응답에 붙이는 기준선입니다.

## 처리 흐름

```text
CommentQueryService.read
  -> 루트 ID limit + 1 조회
  -> 반환할 루트 limit개 선택
  -> 선택한 모든 루트의 전체 서브트리 재귀 조회
  -> 화면 정보 조립 후 한 응답 반환
```

## 핵심 코드

1. [CommentQueryService.java](./src/main/java/cluverse/comment/service/CommentQueryService.java)
2. [CommentReader.java](./src/main/java/cluverse/comment/service/implement/CommentReader.java)
3. [CommentDeletionProcessor.java](./src/main/java/cluverse/comment/service/implement/CommentDeletionProcessor.java)
4. [CommentQueryServiceTest.java](./src/test/java/cluverse/comment/service/CommentQueryServiceTest.java)

## 적용한 리팩토링

- Service는 Repository 대신 댓글 조회·삭제 역할을 표현하는 Implement 경계만 참조합니다.
- 루트 선택과 전체 트리 로딩을 분리해 `limit`이 실제로 제한하는 범위를 코드에서 드러냈습니다.
- 자식 유무에 따른 soft·physical delete 규칙을 삭제 Processor에 모았습니다.

## 검증과 한계

- 테스트는 `limit=20`이어도 루트 하나의 자손 때문에 50,001건이 반환될 수 있음을 보여줍니다.
- Offset이 깊어질수록 루트 선택 비용이 증가합니다.
- 자식 확인과 물리 삭제 사이에 답글이 추가될 수 있으며 두 흐름이 같은 행 잠금을 공유하지 않습니다.
- 최대 depth는 전체 노드 수, 객체 생성, 직렬화와 네트워크 비용을 제한하지 못합니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
