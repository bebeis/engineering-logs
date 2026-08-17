package cluverse.meta.properties;

import java.time.Duration;

public record ViewCountProperties(Duration duplicateTtl, int scanCount) {
}
