package cluverse.place.service.implement;

import cluverse.place.client.PlaceSearchClient;
import cluverse.place.domain.PlaceCandidate;
import cluverse.place.domain.SelectedPlace;
import cluverse.place.service.request.PlaceSelectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaceSelectionResolver {

    private final PlaceSearchClient placeSearchClient;

    public List<SelectedPlace> resolve(List<PlaceSelectionRequest> selections) {
        return selections.stream().map(this::resolve).toList();
    }

    private SelectedPlace resolve(PlaceSelectionRequest selection) {
        PlaceCandidate candidate = placeSearchClient.search(selection.query()).stream()
                .filter(found -> found.sourceFingerprint().equals(selection.sourceFingerprint()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("선택한 장소를 찾을 수 없습니다."));
        return new SelectedPlace(candidate, selection.recommended());
    }
}
