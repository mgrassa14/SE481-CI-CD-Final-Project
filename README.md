# SE481 CI/CD Final Project: Snake

Swing Snake game used for our SE 481 CI/CD case study (trimmed to the minimum the project needs).

- **Build tool:** Maven (`mvn clean package` -> `target/snake-game.jar`)
- **Runtime dependency:** Apache Commons Lang 3 (`ScoreFormatter` pads the score shown in the side panel)
- **Tests:** JUnit 4: `ClockTest`, `DirectionTest`, `TileTypeTest`, `ScoreFormatterTest` (24 tests, no display needed)
- **CI server:** Jenkins on AWS EC2
- **Delivery:** Docker image pushed to Docker Hub

## Run locally
```
mvn clean package
java -jar target/snake-game.jar
```

## Run as the operator (Windows + VcXsrv running with "Disable access control")
```
docker pull DOCKERHUB-USER/snake-game:latest
docker run -it --rm -e DISPLAY=host.docker.internal:0 DOCKERHUB-USER/snake-game:latest
```
