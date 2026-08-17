package cluverse.post.domain;

public record ProcessedPostImage(int displayOrder, Metadata content, Metadata thumbnail) {
    public record Metadata(String objectKey, String contentType, int width, int height, long bytes) {
    }
}
