package cluverse.place.service.implement;

import cluverse.place.domain.Place;
import cluverse.place.domain.PlaceCandidate;
import cluverse.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaceWriter {

    private final PlaceRepository repository;

    public List<Place> upsertAll(List<PlaceCandidate> candidates) {
        return candidates.stream().map(repository::upsert).toList();
    }
}
