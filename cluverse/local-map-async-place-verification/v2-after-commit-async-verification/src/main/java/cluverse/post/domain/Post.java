package cluverse.post.domain;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private final Long id;
    private final List<Long> placeIds = new ArrayList<>();

    public Post(Long id) {
        this.id = id;
    }

    public Long id() {
        return id;
    }

    public void addPlace(Long placeId) {
        placeIds.add(placeId);
    }
}
