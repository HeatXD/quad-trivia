# Quad Trivia

A Spring Boot WebFlux trivia game that fetches questions from [OpenTDB](https://opentdb.com).

A running example can be found at http://trivia.heatxd.dev

## Requirements
- Java 21+
- Maven

## Build
```bash
./mvnw clean package
```

## Run
```bash
./mvnw spring-boot:run
```
or
```bash
java -jar target/quadtrivia-0.0.1-SNAPSHOT.jar
```
and then it should be available at http://localhost:8080 in your browser
