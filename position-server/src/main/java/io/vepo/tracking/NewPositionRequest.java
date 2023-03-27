package io.vepo.tracking;

public record NewPositionRequest(String ownerId, GeoLocation location) {
    
}
