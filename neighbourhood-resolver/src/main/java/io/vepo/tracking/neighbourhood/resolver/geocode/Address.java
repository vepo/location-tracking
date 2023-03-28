package io.vepo.tracking.neighbourhood.resolver.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Address(String amenity,
                      String shop,
                      String place,
                      String railway,
                      @JsonProperty("man_made") String manMade,
                      String tourism,
                      String office,
                      String historic,
                      String junction,
                      String leisure,
                      String building,
                      @JsonProperty("house_number") String houseNumber,
                      String road,
                      String highway,
                      String neighbourhood,
                      String suburb,
                      @JsonProperty("city_district") String cityDistrict,
                      String city,
                      String county,
                      String postcode,
                      String country,
                      @JsonProperty("country_code") String countryCode) {

}
