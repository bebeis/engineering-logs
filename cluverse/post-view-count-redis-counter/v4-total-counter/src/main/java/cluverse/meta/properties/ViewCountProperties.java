package cluverse.meta.properties;

import java.time.Duration;

public record ViewCountProperties(
        Duration duplicateTtl,
        Duration inactiveAfter,
        int scanCount,
        int batchSize,
        Duration initializationLockLease,
        Duration initializationWait,
        int initializationAttempts
) {
}
