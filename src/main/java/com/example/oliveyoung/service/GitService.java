package com.example.oliveyoung.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
public class GitService {

    private static final String REMOTE_URL = "https://github.com/JinsuYeo/hachwimu-iac.git";
    private static final String LOCAL_REPO_PATH = "/app";  // 로컬 저장소 경로
    private static final String BRANCH_NAME = "main";  // 브랜치 이름

    private final CredentialsProvider credentialsProvider;

    public GitService(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    @Async // 비동기적으로 작업을 처리
    public CompletableFuture<String> updateMinReplicas(int replicas) throws GitAPIException, IOException {
        // 1. 로컬 저장소 Clone or Pull
        Git git = pullOrCloneRepository();

        // 2. hpa.yaml 파일 경로
        Path yamlFilePath = Paths.get(LOCAL_REPO_PATH, "hpa.yaml");

        // 3. 파일 수정
        modifyYamlFile(yamlFilePath, replicas);

        // 4. Git Add, Commit, Push
        git.add().addFilepattern("hpa.yaml").call();
        git.commit().setMessage("Updated minReplicas for service-products to " + replicas).call();
        git.push().setCredentialsProvider(credentialsProvider).call();

        return CompletableFuture.completedFuture("Successfully updated minReplicas for service-products to " + replicas + " and pushed to repository.");
    }

    private Git pullOrCloneRepository() throws GitAPIException, IOException {
        File repoDir = new File(LOCAL_REPO_PATH);
        Git git;

        if (repoDir.exists()) {
            // 이미 로컬에 저장소가 존재하는 경우 pull
            git = Git.open(repoDir);
            git.pull().setCredentialsProvider(credentialsProvider).call();
        } else {
            // 로컬에 저장소가 없으면 clone
            git = Git.cloneRepository()
                    .setURI(REMOTE_URL)
                    .setDirectory(repoDir)
                    .setBranch(BRANCH_NAME)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        }

        return git;
    }

    private void modifyYamlFile(Path filePath, int replicas) throws IOException {
        // 파일을 읽어온 후 minReplicas 값을 찾아서 교체
        String content = new String(Files.readAllBytes(filePath));
        String modifiedContent = modifyMinReplicasForProducts(content, replicas);  // 서비스 프로덕트만 변경
        Files.write(filePath, modifiedContent.getBytes());
    }

    // service-products HorizontalPodAutoscaler만을 대상으로 minReplicas 값을 업데이트합니다.
    public static String modifyMinReplicasForProducts(String content, int replicas) {
        // service-products HorizontalPodAutoscaler만을 대상으로 minReplicas 값을 업데이트
        String regex = "(?s)(metadata:\\s*name:\\s*service-hpa-products.*?minReplicas:\\s*)\\d+";
        return content.replaceAll(regex, "$1" + replicas);
    }
}