FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl forgeops-api -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/forgeops-api/target/forgeops-api-*.jar /app/forgeops-api.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/forgeops-api.jar"]

