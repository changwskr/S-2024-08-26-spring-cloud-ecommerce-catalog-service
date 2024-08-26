FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY catalog-service-1.0.jar CatalogService.jar
ENTRYPOINT ["java", "-jar", "CatalogService.jar"]