package cluverse.place.repository;

import cluverse.place.domain.Place;
import cluverse.place.domain.PlaceCandidate;

public interface PlaceRepository {

    Place upsert(PlaceCandidate candidate);
}
