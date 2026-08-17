package cluverse.post.service.implement;

import cluverse.meta.service.implement.PostMetaWriter;
import cluverse.place.domain.Place;
import cluverse.place.domain.SelectedPlace;
import cluverse.place.service.implement.PlaceSelectionResolver;
import cluverse.place.service.implement.PlaceWriter;
import cluverse.post.domain.Post;
import cluverse.post.service.request.PostCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocalMapPostWriteProcessor {

    private final PlaceSelectionResolver placeSelectionResolver;
    private final PlaceWriter placeWriter;
    private final PostWriter postWriter;
    private final PostMetaWriter postMetaWriter;

    @Transactional
    public Long create(Long memberId, PostCreateRequest request) {
        List<SelectedPlace> selectedPlaces = placeSelectionResolver.resolve(request.places());
        List<Place> places = placeWriter.upsertAll(
                selectedPlaces.stream().map(SelectedPlace::candidate).toList());
        Post post = postWriter.create(memberId, request);
        postMetaWriter.createViewCount(post.id());
        places.forEach(place -> post.addPlace(place.id()));
        return post.id();
    }
}
