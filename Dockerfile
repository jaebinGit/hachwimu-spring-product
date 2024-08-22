# Build Stage
FROM openjdk:17-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app

# 빌드에 필요한 패키지 설치 (Git 및 SSH 클라이언트)
RUN apt-get update && apt-get install -y --no-install-recommends git openssh-client ca-certificates && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# SSH 프라이빗 키 추가
ARG SSH_PRIVATE_KEY_BUILD
RUN mkdir -p ~/.ssh && echo "$SSH_PRIVATE_KEY_BUILD" > ~/.ssh/id_rsa && chmod 600 ~/.ssh/id_rsa

# GitHub SSH 키 스캔 및 Known Hosts 등록
RUN ssh-keyscan github.com >> ~/.ssh/known_hosts

# Git 저장소 클론
RUN git clone git@github.com:jaebinGit/hachwimu-spring-product.git .

# 환경 변수 설정 (빌드 시 전달된 값들)
ARG SPRING_APPLICATION_NAME
ARG SPRING_DATASOURCE_WRITER_URL
ARG SPRING_DATASOURCE_READER_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD
ARG SPRING_DATASOURCE_DRIVER_CLASS_NAME
ARG SPRING_JPA_HIBERNATE_DDL_AUTO
ARG SPRING_JPA_SHOW_SQL
ARG SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
ARG SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
ARG SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE
ARG SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT
ARG SPRING_DATASOURCE_HIKARI_MAX_LIFETIME
ARG SPRING_DATA_REDIS_HOST
ARG SPRING_DATA_REDIS_PORT

# Gradle 빌드 실행 (환경변수 활용)
RUN ./gradlew clean build --no-daemon --refresh-dependencies -x test \
    -Dspring.application.name=$SPRING_APPLICATION_NAME \
    -Dspring.datasource.writer.url=$SPRING_DATASOURCE_WRITER_URL \
    -Dspring.datasource.reader.url=$SPRING_DATASOURCE_READER_URL \
    -Dspring.datasource.username=$SPRING_DATASOURCE_USERNAME \
    -Dspring.datasource.password=$SPRING_DATASOURCE_PASSWORD \
    -Dspring.datasource.driver-class-name=$SPRING_DATASOURCE_DRIVER_CLASS_NAME \
    -Dspring.jpa.hibernate.ddl-auto=$SPRING_JPA_HIBERNATE_DDL_AUTO \
    -Dspring.jpa.show-sql=$SPRING_JPA_SHOW_SQL \
    -Dspring.jpa.properties.hibernate.dialect=$SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT \
    -Dspring.datasource.hikari.maximum-pool-size=$SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE \
    -Dspring.datasource.hikari.minimum-idle=$SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE \
    -Dspring.datasource.hikari.idle-timeout=$SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT \
    -Dspring.datasource.hikari.max-lifetime=$SPRING_DATASOURCE_HIKARI_MAX_LIFETIME \
    -Dspring.data.redis.host=$SPRING_DATA_REDIS_HOST \
    -Dspring.data.redis.port=$SPRING_DATA_REDIS_PORT

# Production Stage
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/oliveyoung-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]