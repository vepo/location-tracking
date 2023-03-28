package io.vepo.tracking.neighbourhood.resolver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.location.tracking.position.Address;
import io.vepo.location.tracking.position.EnrichmentResult;
import io.vepo.location.tracking.position.Neighbourhood;
import io.vepo.location.tracking.position.NewPosition;
import io.vepo.location.tracking.position.PositionWithNeighbourhood;
import io.vepo.location.tracking.position.Status;
import io.vepo.tracking.neighbourhood.resolver.geocode.GeoCodeResponse;

@ApplicationScoped
public class TopologyProducer {
    private static final Logger logger = LoggerFactory.getLogger(TopologyProducer.class);

    private static ObjectMapper mapper = new ObjectMapper();

    private static EnrichmentResult success(NewPosition input, PositionWithNeighbourhood output) {
        return EnrichmentResult.newBuilder()
                               .setInput(input)
                               .setOutput(output)
                               .setStatus(Status.SUCCESS)
                               .build();
    }

    private static EnrichmentResult fail(NewPosition input) {
        return EnrichmentResult.newBuilder()
                               .setInput(input)
                               .setStatus(Status.FAIL)
                               .build();
    }

    private static PositionWithNeighbourhood toAvro(NewPosition value, GeoCodeResponse geoCodeResponse) {
        return PositionWithNeighbourhood.newBuilder()
                                        .setOwnerId(value.getOwnerId())
                                        .setLocation(value.getLocation())
                                        .setNeighbourhood(Neighbourhood.newBuilder()
                                                                       .setPlaceId(geoCodeResponse.placeId())
                                                                       .setLicence(geoCodeResponse.licence())
                                                                       .setPoweredBy(geoCodeResponse.poweredBy())
                                                                       .setOsmType(geoCodeResponse.osmType())
                                                                       .setOsmId(geoCodeResponse.osmId())
                                                                       .setLat(geoCodeResponse.lat())
                                                                       .setLon(geoCodeResponse.lon())
                                                                       .setDisplayName(geoCodeResponse.displayName())
                                                                       .setAddress(Address.newBuilder()
                                                                                          .setShop(geoCodeResponse.address()
                                                                                                                  .shop())
                                                                                          .setAmenity(geoCodeResponse.address()
                                                                                                                     .amenity())
                                                                                          .setTourism(geoCodeResponse.address()
                                                                                                                     .tourism())
                                                                                          .setJunction(geoCodeResponse.address()
                                                                                                                      .junction())
                                                                                          .setPlace(geoCodeResponse.address()
                                                                                                                   .place())
                                                                                          .setRailway(geoCodeResponse.address()
                                                                                                                     .railway())
                                                                                          .setManMade(geoCodeResponse.address()
                                                                                                                     .manMade())
                                                                                          .setOffice(geoCodeResponse.address()
                                                                                                                    .office())
                                                                                          .setHistoric(geoCodeResponse.address()
                                                                                                                      .historic())
                                                                                          .setLeisure(geoCodeResponse.address()
                                                                                                                     .leisure())
                                                                                          .setBuilding(geoCodeResponse.address()
                                                                                                                      .building())
                                                                                          .setHouseNumber(geoCodeResponse.address()
                                                                                                                         .houseNumber())
                                                                                          .setRoad(geoCodeResponse.address()
                                                                                                                  .road())
                                                                                          .setHighway(geoCodeResponse.address()
                                                                                                                     .highway())
                                                                                          .setNeighbourhood(geoCodeResponse.address()
                                                                                                                           .neighbourhood())
                                                                                          .setSuburb(geoCodeResponse.address()
                                                                                                                    .suburb())
                                                                                          .setCity(geoCodeResponse.address()
                                                                                                                  .city())
                                                                                          .setCityDistrict(geoCodeResponse.address()
                                                                                                                          .cityDistrict())
                                                                                          .setCounty(geoCodeResponse.address()
                                                                                                                    .county())
                                                                                          .setCountry(geoCodeResponse.address()
                                                                                                                     .country())
                                                                                          .setPostcode(geoCodeResponse.address()
                                                                                                                      .postcode())
                                                                                          .setCountryCode(geoCodeResponse.address()
                                                                                                                         .countryCode())
                                                                                          .build())
                                                                       .setBoundingbox(Arrays.stream(geoCodeResponse.boundingBox())
                                                                                             .mapToObj(point -> point)
                                                                                             .toList())
                                                                       .build())
                                        .build();
    }

    @Produces
    public Topology buildTopology() {
        var builder = new StreamsBuilder();

        builder.<String, NewPosition>stream("positions")
               .map((key, value) -> {
                   logger.info("Received data! key={} value={}", key, value);
                   var request = HttpRequest.newBuilder()
                                            .uri(URI.create(String.format("https://geocode.maps.co/reverse?lat=%f&lon=%f",
                                                                          value.getLocation().getLat(),
                                                                          value.getLocation().getLon())))
                                            .header("Accept", "application/json")
                                            .GET().build();
                   try {
                       var response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
                       if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                           var geoCodeResponse = mapper.readValue(response.body(), GeoCodeResponse.class);
                           logger.info("Returning success! geoCode={}", geoCodeResponse);
                           return KeyValue.pair(key, success(value, toAvro(value, geoCodeResponse)));
                       } else {
                           logger.info("Returning fail! statusCode={}", response.statusCode());
                           return KeyValue.pair(key, fail(value));
                       }
                   } catch (IOException ex) {
                       logger.error("Error sending HTTP Request", ex);
                       return KeyValue.pair(key, fail(value));
                   } catch (InterruptedException e) {
                       Thread.currentThread().interrupt();
                       return KeyValue.pair(key, fail(value));
                   }
               })
               .split()
               .branch((key, value) -> value.getStatus() == Status.SUCCESS,
                       Branched.withConsumer(stream -> stream.map((key, value) -> KeyValue.pair(key, value.getOutput()))
                                                             .to("position-with-neighbourhood")))
               .defaultBranch(Branched.withConsumer(stream -> stream.map((key,
                                                                          value) -> KeyValue.pair(key,
                                                                                                  value.getInput()))
                                                                    .to("position-without-neighbourhood")));

        return builder.build();
    }
}
