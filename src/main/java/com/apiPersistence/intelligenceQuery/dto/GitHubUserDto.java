package com.apiPersistence.intelligenceQuery.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUserDto (
        Long id,
        String login,
        String name,
        String email,
        @JsonProperty("avatar_url") String avatarUrl
) {}
