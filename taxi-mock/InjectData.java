
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.opencsv:opencsv:4.1
//DEPS com.fasterxml.jackson.core:jackson-databind:2.14.2

import static java.util.Objects.nonNull;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

public class InjectData {
    private static final String COORD_REGEX = "\\[([\\-0-9\\.]+),([\\-0-9\\.]+)\\]";
    private static final Pattern COORD_PATTERN = Pattern.compile(COORD_REGEX);

    public static void main(String[] args) throws IOException, InterruptedException {
        var mapper = new ObjectMapper();
        try (var reader = new CSVReader(new FileReader("all.csv"))) {
            reader.readNext();// skip header
            String[] tripData;
            while (nonNull(tripData = reader.readNext())) {
                var matcher = COORD_PATTERN.matcher(tripData[8]);
                while (matcher.find()) {
                    var trip = mapper.createObjectNode();
                    trip.put("ownerId", tripData[4]);
                    trip.put("startedAt", LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(tripData[5])),
                                                                  ZoneId.systemDefault())
                                                       .toString());
                    var location = mapper.createObjectNode();
                    location.put("lon", Double.parseDouble(matcher.group(1)));
                    location.put("lat", Double.parseDouble(matcher.group(2)));
                    trip.set("location", location);

                    System.out.println(trip.toPrettyString());
                    var process = new ProcessBuilder("docker",
                                                     "exec", "location-tracking_position-server_1",
                                                     "curl", "-X", "POST", "localhost:8080/position",
                                                     "-H", "Content-Type: application/json",
                                                     "-d", trip.toPrettyString()).start();

                    process.waitFor();
                    System.out.println(new String(process.getInputStream().readAllBytes()));
                }
            }
        }
    }
}
