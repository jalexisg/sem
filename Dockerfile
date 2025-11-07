# Runtime-only Dockerfile
# This Dockerfile expects the application JAR to be built locally (target/sem.jar).
# Build the JAR before building the image:
#   mvn -DskipTests package
# Then build the image:
#   docker build -t sem .

FROM amazoncorretto:17
WORKDIR /tmp
# Copy the pre-built jar from the build context. Ensure `mvn package` produced target/sem.jar
COPY target/sem.jar /tmp/sem.jar
ENTRYPOINT ["java", "-jar", "sem.jar", "db:3306", "30000"]