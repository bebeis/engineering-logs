package cluverse.place.client;

import cluverse.place.domain.PlaceCandidate;

import java.util.List;

public interface PlaceSearchClient {

    List<PlaceCandidate> search(String query);
}
