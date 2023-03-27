package io.vepo.tracking;

import java.time.LocalDateTime;

public record NewPositionRequest(String ownerId, LocalDateTime startedAt, GeoLocation location) {
    
}
