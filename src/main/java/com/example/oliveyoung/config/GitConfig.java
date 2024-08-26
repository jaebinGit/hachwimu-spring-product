package com.example.oliveyoung.config;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitConfig {

    @Value("${GIT_TOKEN}")
    private String gitToken;

    @Bean
    public CredentialsProvider credentialsProvider() {
        if (gitToken == null || gitToken.isEmpty()) {
            throw new IllegalArgumentException("GIT_TOKEN must not be null or empty");
        }
        return new UsernamePasswordCredentialsProvider("jaebinGit", gitToken);
    }
}
