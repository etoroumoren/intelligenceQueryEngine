package com.apiPersistence.intelligenceQuery.dto;

public record GitHubEmailDto(
        String email,
        Boolean primary,
        Boolean verified,
        String visibility
) {}
