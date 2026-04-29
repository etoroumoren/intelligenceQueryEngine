package com.apiPersistence.intelligenceQuery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubTokenResponseDto(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("scope") String scope
) {}
