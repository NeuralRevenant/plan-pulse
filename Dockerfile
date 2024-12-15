FROM --platform=linux/amd64 eclipse-temurin:21-jdk-jammy
ARG JAR_FILE=build/libs/planpulse.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
