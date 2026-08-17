package cluverse.post.service.implement;

import cluverse.place.domain.Place;
import cluverse.place.domain.SelectedPlace;
import cluverse.place.service.implement.PlaceWriter;
import cluverse.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostPlaceCompletionProcessor {

    private final PostAccessReader postAccessReader;
    private final PlaceWriter placeWriter;

    @Transactional
    public void complete(Long postId, List<SelectedPlace> selectedPlaces) {
        Post post = postAccessReader.readOrThrow(postId);
        List<Place> places = placeWriter.upsertAll(
                selectedPlaces.stream().map(SelectedPlace::candidate).toList());
        places.forEach(place -> post.addPlace(place.id()));
    }
}
