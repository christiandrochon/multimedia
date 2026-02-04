# Étape 1 : Build (Maven + JDK déjà inclus)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# Étape 2 : Runtime (plus léger qu'un JDK)
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8087

ENV BASE_DIRECTORY=/app/ferecatu
ENV BASE_OUTPUTS_DIRECTORY=/app/outputs
ENV IMAGES_BASE_URL=http://multimedias-server:8087/images

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
