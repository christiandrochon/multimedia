# Étape 1 : Construire l'application avec Maven
FROM openjdk:21-jdk-slim AS build
RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B  # Télécharger les dépendances

COPY src ./src
RUN mvn clean package -DskipTests  # Construire l'application

# Étape 2 : Image finale
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar /app/app.jar

# Exposer le port utilisé par l'application
EXPOSE 8087

# Configurer les variables d'environnement pour le backend
ENV BASE_DIRECTORY=/app/ferecatu
ENV BASE_OUTPUTS_DIRECTORY=/app/outputs
ENV IMAGES_BASE_URL=http://multimedias-server:8087/images

# Commande pour démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]



#FROM openjdk:21-jdk-slim AS build
#RUN apt-get update && apt-get install -y maven
#
## Copier et construire l'application
#WORKDIR /app
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## Étape 2 : Image finale
#FROM openjdk:21-jdk-slim
#WORKDIR /app
#COPY --from=build /app/target/*.jar /app/app.jar
#EXPOSE 8087
#
## Ajouter une variable pour les fichiers multimédia
#ENV BASE_DIRECTORY=/app/ferecatu
#
#ENTRYPOINT ["java", "-jar", "app.jar"]