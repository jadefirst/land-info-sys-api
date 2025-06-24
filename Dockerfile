FROM openjdk:21-jdk-slim AS builder

# Gradle 수동 설치
RUN apt-get update && apt-get install -y wget unzip
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip
RUN unzip gradle-8.5-bin.zip
RUN mv gradle-8.5 /opt/gradle
ENV PATH="/opt/gradle/bin:${PATH}"

WORKDIR /app
COPY . .

# Java 21과 호환되는 Gradle로 빌드
RUN gradle clean build --no-daemon

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]