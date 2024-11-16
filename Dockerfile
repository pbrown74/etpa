FROM openjdk:17-oracle
EXPOSE 9999
ADD target/etpa-0.0.1-SNAPSHOT.jar etpa-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/etpa-0.0.1.jar"]
