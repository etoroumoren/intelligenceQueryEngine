package com.apiPersistence.intelligenceQuery.entity;


import com.apiPersistence.intelligenceQuery.uuidGenerator.Uuidv7;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


@Data
@Entity
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Table(
        name = "profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_profile_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "gender_index", columnList = "gender"),
                @Index(name = "age_group_index", columnList = "age_group"),
                @Index(name = "country_id_index", columnList = "country_id"),
                @Index(name = "age_index", columnList = "age"),
                @Index(name = "created_at_index", columnList = "created_at")
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Uuidv7
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "gender")
    private String gender;

    @Column(name = "gender_probability")
    private Double genderProbability;

    @Column(name = "age")
    private Integer age;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "country_id", length = 2)
    private String countryId;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "country_probability")
    private Double countryProbability;

    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            timezone = "UTC")
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if(createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
