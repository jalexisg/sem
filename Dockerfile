# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -f pom.xml clean package

# Stage 2: Create the final image
FROM amazoncorretto:17
COPY --from=build /app/target/*.jar /tmp/sem.jar
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "sem.jar", "db:3306", "30000"]