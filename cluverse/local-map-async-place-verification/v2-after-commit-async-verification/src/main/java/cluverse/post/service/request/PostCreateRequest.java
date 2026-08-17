package cluverse.post.service.request;

import cluverse.place.domain.SelectedPlace;

import java.util.List;

public record PostCreateRequest(
        String requestId,
        String title,
        String content,
        List<SelectedPlace> places
) {
    public PostCreateRequest {
        places = List.copyOf(places);
    }
}
