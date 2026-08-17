package cluverse.post.service.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PostListRequest(
        @NotNull Long boardId,
        @Min(1) @Max(200) Integer page,
        @Min(1) @Max(100) Integer size
) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    public int pageOrDefault() {
        return page == null ? DEFAULT_PAGE : page;
    }

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public long offset() {
        return (long) (pageOrDefault() - 1) * sizeOrDefault();
    }

    public long lastRequiredIndex() {
        return offset() + sizeOrDefault();
    }
}
