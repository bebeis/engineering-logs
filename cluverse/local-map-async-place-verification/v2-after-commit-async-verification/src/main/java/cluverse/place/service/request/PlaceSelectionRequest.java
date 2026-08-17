package cluverse.place.service.request;

public record PlaceSelectionRequest(
        String query,
        String sourceFingerprint,
        boolean recommended
) {
}
