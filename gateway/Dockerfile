FROM amazoncorretto:11-alpine-jdk
COPY target/shareit-gateway-0.0.1-SNAPSHOT-exec.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]