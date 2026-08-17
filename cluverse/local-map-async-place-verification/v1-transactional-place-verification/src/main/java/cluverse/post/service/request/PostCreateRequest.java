package cluverse.post.service.request;

import cluverse.place.service.request.PlaceSelectionRequest;

import java.util.List;

public record PostCreateRequest(String title, String content, List<PlaceSelectionRequest> places) {
    public PostCreateRequest {
        places = List.copyOf(places);
    }
}
