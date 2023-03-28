package io.vepo.tracking.neighbourhood.resolver.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeoCodeResponse(@JsonProperty("place_id") long placeId,
                              String licence,
                              @JsonProperty("powered_by") String poweredBy,
                              @JsonProperty("osm_type") String osmType,
                              @JsonProperty("osm_id") long osmId,
                              double lat,
                              double lon,
                              @JsonProperty("display_name") String displayName,
                              Address address,
                              @JsonProperty("boundingbox") double[] boundingBox) {

}
