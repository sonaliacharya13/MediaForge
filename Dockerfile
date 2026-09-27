FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/mediaforge-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

RUN apt-get update && \
    apt-get install -y imagemagick ffmpeg && \
    rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["java", "-jar", "app.jar"]