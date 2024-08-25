package com.example.oliveyoung.controller;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Scale;
import io.kubernetes.client.openapi.models.V1ScaleSpec;
import io.kubernetes.client.util.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/scale")
public class ScalingController {

    private final AppsV1Api appsV1Api;

    @Value("${kubernetes.namespace}")
    private String namespace;

    @Value("${kubernetes.deployment.name}")
    private String deploymentName;

    public ScalingController(
            @Value("${kubernetes.api.server.url}") String apiServerUrl,
            @Value("${kubernetes.token.path}") String tokenPath) throws Exception {
        // Kubernetes 클라이언트 설정
        ApiClient client = Config.fromToken(apiServerUrl, tokenPath, false);
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        this.appsV1Api = new AppsV1Api();
    }

    @PostMapping
    public String scaleDeployment(@RequestParam int replicas) {
        try {
            // V1ScaleSpec 객체 생성 및 replicas 값 설정
            V1ScaleSpec scaleSpec = new V1ScaleSpec();
            scaleSpec.setReplicas(replicas);

            // V1Scale 객체 생성 및 spec 설정
            V1Scale scale = new V1Scale();
            scale.setSpec(scaleSpec);

            // Kubernetes API 호출로 Deployment 스케일 조정
            appsV1Api.replaceNamespacedDeploymentScale(deploymentName, namespace, scale, null, null, null, null);
            return "Deployment scaled successfully to " + replicas + " replicas.";
        } catch (ApiException e) {
            e.printStackTrace();
            return "Error scaling deployment: " + e.getResponseBody();
        }
    }
}