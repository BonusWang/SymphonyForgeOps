FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -Dmaven.repo.local=/root/.m2/repository -pl forgeops-api -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/forgeops-api/target/forgeops-api-*.jar /app/forgeops-api.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/forgeops-api.jar"]
