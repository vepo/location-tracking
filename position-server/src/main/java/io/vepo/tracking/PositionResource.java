package io.vepo.tracking;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vepo.location.tracking.position.GeoLocation;
import io.vepo.location.tracking.position.NewPosition;

@Path("/position")
public class PositionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionResource.class);

    @Inject
    @Channel("positions")
    Emitter<NewPosition> positionsEmitter;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PositionResponse newPositionHandle(NewPositionRequest request) {
        LOGGER.info("New position created! request={}", request);
        positionsEmitter.send(KafkaRecord.of(request.ownerId(), NewPosition.newBuilder()
                                                                           .setOwnerId(request.ownerId())
                                                                           .setLocation(GeoLocation.newBuilder()
                                                                                                   .setLat(request.location()
                                                                                                                  .lat())
                                                                                                   .setLon(request.location()
                                                                                                                  .lon())
                                                                                                   .build())
                                                                           .build()));
        return new PositionResponse();
    }
}