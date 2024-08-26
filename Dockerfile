# Build Stage
FROM openjdk:17-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app/repo

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

# Remove sensitive data after cloning
RUN rm -rf ~/.ssh/id_rsa

# Copy Gradle wrapper files and build project
COPY ./gradlew ./gradlew
COPY ./gradle ./gradle

# Gradle 빌드 실행 (환경변수 활용)
RUN ./gradlew clean build --no-daemon --refresh-dependencies -x test

# Production Stage
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app/repo

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/repo/build/libs/oliveyoung-0.0.1-SNAPSHOT.jar app.jar

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
ARG GIT_TOKEN
# Kubernetes related ARGs
ARG KUBERNETES_API_SERVER_URL
ARG KUBERNETES_NAMESPACE
ARG KUBERNETES_DEPLOYMENT_NAME
ARG KUBERNETES_TOKEN_PATH


# 환경 변수 설정 (애플리케이션 실행 시 사용)
ENV SPRING_APPLICATION_NAME=${SPRING_APPLICATION_NAME}
ENV SPRING_DATASOURCE_WRITER_URL=${SPRING_DATASOURCE_WRITER_URL}
ENV SPRING_DATASOURCE_READER_URL=${SPRING_DATASOURCE_READER_URL}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=${SPRING_DATASOURCE_DRIVER_CLASS_NAME}
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=${SPRING_JPA_HIBERNATE_DDL_AUTO}
ENV SPRING_JPA_SHOW_SQL=${SPRING_JPA_SHOW_SQL}
ENV SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT}
ENV SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE}
ENV SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE}
ENV SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=${SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT}
ENV SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=${SPRING_DATASOURCE_HIKARI_MAX_LIFETIME}
ENV SPRING_DATA_REDIS_HOST=${SPRING_DATA_REDIS_HOST}
ENV SPRING_DATA_REDIS_PORT=${SPRING_DATA_REDIS_PORT}
ENV GIT_TOKEN=${GIT_TOKEN}

# Kubernetes related ENVs
ENV KUBERNETES_API_SERVER_URL=${KUBERNETES_API_SERVER_URL}
ENV KUBERNETES_NAMESPACE=${KUBERNETES_NAMESPACE}
ENV KUBERNETES_DEPLOYMENT_NAME=${KUBERNETES_DEPLOYMENT_NAME}
ENV KUBERNETES_TOKEN_PATH=${KUBERNETES_TOKEN_PATH}

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

#test12345678