# Java 21 runtime (runs our Java 17 bytecode)
FROM eclipse-temurin:21-jre

# Snake is a Swing (GUI) game. Containers are headless by default,
# so install the X11 libraries Java needs to draw a window.
RUN apt-get update && apt-get install -y --no-install-recommends \
    libxtst6 libxrender1 libxi6 libfreetype6 libxext6 libx11-6 libxxf86vm1 fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# The fat JAR that Maven (shade plugin) built: game + Apache Commons Lang inside
COPY target/snake-game.jar snake-game.jar

ENTRYPOINT ["java", "-jar", "snake-game.jar"]
