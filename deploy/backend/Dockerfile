FROM eclipse-temurin:17-jre

WORKDIR /app

COPY jiangnan-travel-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
