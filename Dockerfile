FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/bank-rest-0.0.1-SNAPSHOT.jar /app/bank_rest.jar

ENTRYPOINT ["java", "-jar", "bank_rest.jar"]