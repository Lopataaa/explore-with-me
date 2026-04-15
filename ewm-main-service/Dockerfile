FROM eclipse-temurin:21-jre-jammy
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "/app.jar"]
