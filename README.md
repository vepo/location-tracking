# Location Tracking

This is a location tracking system, each individual will receive an ID and its position will be tracked given some useful information like history, heatmap and position advices.

## Systems

1. Position Server

   This is a HTTP server that will receive a position from a individual.

2. Neighbourhood Resolver

   This is a Kafka Stream projects that enrich the data with neighbourhood information

## Testing

If you don't use `--build` it will not identify your changes.

```bash
docker-compose stop && docker-compose rm -f && docker-compose up --build -d
```

```bash
docker exec -it location-tracking_position-server_1 curl -X POST localhost:8080/position  -H 'Content-Type: application/json' -d '{"ownerId": "12313", "location": {"lat": 124, "lon":112321}}'
```