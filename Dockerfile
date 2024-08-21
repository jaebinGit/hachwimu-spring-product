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

# 빌드 실행
RUN ./gradlew clean build --no-daemon --refresh-dependencies -x test

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