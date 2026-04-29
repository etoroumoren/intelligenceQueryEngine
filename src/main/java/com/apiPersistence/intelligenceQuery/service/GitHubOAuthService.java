package com.apiPersistence.intelligenceQuery.service;


import com.apiPersistence.intelligenceQuery.dto.GitHubEmailDto;
import com.apiPersistence.intelligenceQuery.dto.GitHubTokenResponseDto;
import com.apiPersistence.intelligenceQuery.dto.GitHubUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class GitHubOAuthService {

    private static final String GITHUB_OAUTH_BASE = "https://github.com";
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "2022-11-28";

    private final WebClient.Builder webClientBuilder;

    @Value("${app.github.client-id}")
    private String clientId;

    @Value("${app.github.client-secret}")
    private String clientSecret;

    @Value("${app.github.redirect-uri}")
    private String redirectUri;

    public String exchangeCodeForAccessToken(String code) {
        if(!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("GitHub OAuth code must not be empty");
        }

        WebClient client = webClientBuilder
                .baseUrl(GITHUB_OAUTH_BASE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        GitHubTokenResponseDto response;

        try {
            response = client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/login/oauth/access_token")
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("code", code)
                            .queryParam("redirect_uri", redirectUri)
                            .build())
                    .retrieve()
                    .bodyToMono(GitHubTokenResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new IllegalStateException("GitHub token exchange failed: " + e.getStatusCode(), e);
        }

        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new IllegalStateException("GitHub token exchange returned no access token");
        }

        return response.accessToken();
    }

    public GitHubUserDto fetchUser(String githubAccessToken) {
        WebClient client = githubApiClient(githubAccessToken);
        try {
            GitHubUserDto user = client.get()
                    .uri("/user")
                    .retrieve()
                    .bodyToMono(GitHubUserDto.class)
                    .block();

            if (user == null || user.id() == null || !StringUtils.hasText(user.login())) {
                throw new IllegalStateException("GitHub user payload is incomplete");
            }
            return user;
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("Failed to fetch GitHub user: " + ex.getStatusCode(), ex);
        }
    }

    public String fetchPrimaryEmail(String githubAccessToken) {
        WebClient client = githubApiClient(githubAccessToken);
        try {
            List<GitHubEmailDto> emails = client.get()
                    .uri("/user/emails")
                    .retrieve()
                    .bodyToFlux(GitHubEmailDto.class)
                    .collectList()
                    .block();

            if (emails == null || emails.isEmpty()) {
                return null;
            }

            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.primary()) && Boolean.TRUE.equals(e.verified()) && StringUtils.hasText(e.email()))
                    .map(GitHubEmailDto::email)
                    .findFirst()
                    .or(() -> emails.stream()
                            .filter(e -> Boolean.TRUE.equals(e.verified()) && StringUtils.hasText(e.email()))
                            .min(Comparator.comparing(e -> Boolean.TRUE.equals(e.primary()) ? 0 : 1))
                            .map(GitHubEmailDto::email))
                    .orElse(null);
        } catch (WebClientResponseException ex) {
            // Some users keep email private / scope missing; don't fail auth for this.
            return null;
        }
    }

    private WebClient githubApiClient(String githubAccessToken) {
        if (!StringUtils.hasText(githubAccessToken)) {
            throw new IllegalArgumentException("GitHub access token must not be empty");
        }

        return webClientBuilder
                .baseUrl(GITHUB_API_BASE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubAccessToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", GITHUB_API_VERSION)
                .build();
    }
}
