package cluverse.home.service.implement;

import java.util.List;
import java.util.Map;

public interface PostAccessReader {
    Map<Long, String> readAccessibleTitles(long memberId, List<Long> candidatePostIds);
}
