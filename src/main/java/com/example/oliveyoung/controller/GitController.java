package com.example.oliveyoung.controller;

import com.example.oliveyoung.service.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/products")
public class GitController {

    private final GitService gitService;

    public GitController(GitService gitService) {
        this.gitService = gitService;
    }

    @PostMapping("/pod/scale")
    public CompletableFuture<ResponseEntity<String>> podScaleService(@RequestParam int replicas) throws GitAPIException, IOException {
        return gitService.updatePodMinReplicas(replicas)
                .thenApply(ResponseEntity::ok) // 비동기 작업 성공 시 응답 처리
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + ex.getMessage())); // 비동기 작업 실패 시 예외 처리
    }

    @PostMapping("/node/scale")
    public CompletableFuture<ResponseEntity<String>> nodScaleService(@RequestParam int replicas) throws GitAPIException, IOException {
        return gitService.updateNodeMinReplicas(replicas)
                .thenApply(ResponseEntity::ok) // 비동기 작업 성공 시 응답 처리
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + ex.getMessage())); // 비동기 작업 실패 시 예외 처리
    }
}
