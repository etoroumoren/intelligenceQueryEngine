package com.apiPersistence.intelligenceQuery.entity;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String name,
        String gender,
        Double gender_probability,
        Integer age,
        String age_group,
        String country_id,
        String country_name,
        Double country_probability,
        String created_at
) {
    public static ProfileResponse from(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getName(),
                p.getGender(),
                p.getGenderProbability(),
                p.getAge(),
                p.getAgeGroup(),
                p.getCountryId(),
                p.getCountryName(),
                p.getCountryProbability(),
                p.getCreatedAt() != null
                        ? p.getCreatedAt()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                        : null
        );
    }
}