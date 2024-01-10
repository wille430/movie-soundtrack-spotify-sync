FROM eclipse-temurin:17.0.9_9-jdk-jammy

ENV DATA_DIRECTORY="/etc/msss"

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:resolve

COPY src ./src

RUN ./mvnw clean package

CMD [ "java", "-jar", "./target/movie-soundtrack-spotify-sync-1.0-SNAPSHOT.jar" ]
