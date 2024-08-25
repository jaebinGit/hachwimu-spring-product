package com.example.oliveyoung.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/scale")
public class ScalingController {

    // 환경 변수로부터 Kubernetes API 서버 URL, 네임스페이스, 배포 이름, 토큰 경로를 가져옴
    @Value("${kubernetes.api.server.url}")
    private String apiServerUrl;

    @Value("${kubernetes.namespace}")
    private String namespace;

    @Value("${kubernetes.deployment.name}")
    private String deploymentName;

    @Value("${kubernetes.token.path}")
    private String tokenPath;

    // POST 요청을 처리하는 메서드. replica 수를 조정함.
    @PostMapping
    public String scaleDeployment(@RequestParam int replicas) {
        String token;
        try {
            // 파일 시스템에서 Kubernetes 서비스 계정 토큰을 읽어옴 (Kubernetes 시크릿에서 제공됨)
            token = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream(tokenPath))).readLine().trim();
        } catch (Exception e) {
            return "Kubernetes 토큰을 읽는 중 오류 발생: " + e.getMessage();
        }

        // `curl` 명령어 생성
        String curlCommand = String.format(
                "curl -X PATCH -H \"Authorization: Bearer %s\" -H \"Content-Type: application/strategic-merge-patch+json\" "
                        + "\"%s/apis/apps/v1/namespaces/%s/deployments/%s/scale\" -d '{\"spec\": {\"replicas\": %d}}' -k",
                token, apiServerUrl, namespace, deploymentName, replicas
        );

        try {
            // 생성된 명령어를 실행
            Process process = Runtime.getRuntime().exec(curlCommand);
            process.waitFor();

            // curl 명령어의 출력 결과를 읽어옴
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString(); // 명령어 실행 결과 반환
        } catch (Exception e) {
            return "배포 스케일링 중 오류 발생: " + e.getMessage(); // 오류 발생 시 메시지 반환
        }
    }
}
