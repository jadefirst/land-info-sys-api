FROM gradle:7.6-jdk17 AS builder

WORKDIR /app
COPY . .

# Gradle 빌드 (의존성 다운로드 포함)
RUN gradle clean build --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]