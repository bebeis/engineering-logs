package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularityPolicy;
import cluverse.popularity.repository.BoardPopularityPolicyRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

public class PopularityPolicyRefreshProcessor {

    private final PopularityPolicySampleReader sampleReader;
    private final BoardPopularityPolicyRepository policyRepository;
    private final PopularityPolicyStore policyStore;
    private final BoardPopularityPolicyCalculator calculator;
    private final PopularityProperties properties;
    private final Clock clock;

    public PopularityPolicyRefreshProcessor(
            PopularityPolicySampleReader sampleReader,
            BoardPopularityPolicyRepository policyRepository,
            PopularityPolicyStore policyStore,
            BoardPopularityPolicyCalculator calculator,
            PopularityProperties properties,
            Clock clock
    ) {
        this.sampleReader = sampleReader;
        this.policyRepository = policyRepository;
        this.policyStore = policyStore;
        this.calculator = calculator;
        this.properties = properties;
        this.clock = clock;
    }

    public int refresh() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        LocalDateTime sampleEnd = now.minus(properties.promotionWindow());
        LocalDateTime sampleStart = sampleEnd.minus(properties.policySampleWindow());
        int refreshed = 0;

        for (long boardId : sampleReader.readBoardIds(sampleStart, sampleEnd)) {
            try {
                var samples = sampleReader.readSamples(boardId, sampleStart, sampleEnd);
                Optional<PopularityPolicy> previous = policyRepository.findByBoardId(boardId)
                        .map(stored -> stored.policy());
                var calculation = calculator.calculate(samples, previous);
                policyStore.replace(
                        boardId,
                        calculation.policy(),
                        samples.size(),
                        calculation.source(),
                        now
                );
                refreshed++;
            } catch (RuntimeException ignored) {
                // 한 게시판의 실패가 다른 게시판 정책 갱신을 막지 않는다.
            }
        }
        return refreshed;
    }
}
