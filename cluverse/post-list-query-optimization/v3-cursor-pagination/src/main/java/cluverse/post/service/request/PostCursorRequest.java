package cluverse.post.service.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostCursorRequest(
        @NotNull Long boardId,
        @Min(1) @Max(100) Integer size,
        LocalDate date,
        LocalDateTime cursorCreatedAt,
        Long cursorPostId,
        PostCursorDirection direction
) {
    private static final int DEFAULT_SIZE = 20;
    private static final PostCursorDirection DEFAULT_DIRECTION = PostCursorDirection.NEXT;

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public PostCursorDirection directionOrDefault() {
        return direction == null ? DEFAULT_DIRECTION : direction;
    }

    public boolean hasCursor() {
        return cursorCreatedAt != null && cursorPostId != null;
    }

    public boolean isDateAnchored() {
        return date != null;
    }

    public boolean isPreviousMove() {
        return hasCursor() && directionOrDefault() == PostCursorDirection.PREV;
    }

    public LocalDateTime exclusiveDateEnd() {
        return date.plusDays(1).atStartOfDay();
    }

    @AssertTrue(message = "cursorCreatedAt과 cursorPostId는 함께 입력해야 합니다.")
    public boolean isCursorPairComplete() {
        return (cursorCreatedAt == null) == (cursorPostId == null);
    }

    @AssertTrue(message = "날짜 앵커와 커서는 함께 사용할 수 없습니다.")
    public boolean isDateCursorExclusive() {
        return date == null || !hasCursor();
    }

    @AssertTrue(message = "이동 방향은 커서와 함께 사용해야 합니다.")
    public boolean isDirectionWithCursor() {
        return direction == null || hasCursor();
    }
}
