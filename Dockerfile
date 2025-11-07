FROM amazoncorretto:17
COPY ./target/seMethods-0.1.0.2.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "seMethods-0.1.0.2.jar", "db:3306", "30000"]