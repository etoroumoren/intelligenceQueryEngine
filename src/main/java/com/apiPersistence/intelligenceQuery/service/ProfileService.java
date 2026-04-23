package com.apiPersistence.intelligenceQuery.service;


import com.apiPersistence.intelligenceQuery.entity.Profile;
import com.apiPersistence.intelligenceQuery.entity.ProfileResponse;
import com.apiPersistence.intelligenceQuery.repository.ProfileRepository;
import com.apiPersistence.intelligenceQuery.repository.ProfileSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Map<String, Object> getProfiles(
            String gender,
            String ageGroup,
            String countryId,
            Integer minAge,
            Integer maxAge,
            Double minGenderProb,
            Double minCountryProb,
            String sortBy,
            String order,
            int page,
            int limit
    ) {
        // normalize
        if (gender != null) gender = gender.trim().toLowerCase();
        if (ageGroup != null) ageGroup = ageGroup.trim().toLowerCase();
        if (countryId != null) countryId = countryId.trim().toUpperCase();
        if (sortBy != null) sortBy = sortBy.trim().toLowerCase();
        if (order != null) order = order.trim().toLowerCase();

        // validate
        if (page < 1) {
            throw new IllegalArgumentException("Invalid query parameters");
        }
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("Invalid query parameters");
        }
        if (gender != null && !gender.equals("male") && !gender.equals("female")) {
            throw new IllegalArgumentException("Invalid query parameters");
        }

        boolean validSortBy = sortBy == null || sortBy.equals("age") || sortBy.equals("created_at") || sortBy.equals("gender_probability");
        boolean validOrder = order == null || order.equals("asc") || order.equals("desc");
        if (!validSortBy || !validOrder) {
            throw new IllegalArgumentException("Invalid query parameters");
        }

        Specification<Profile> specification =
                ProfileSpecification.hasGender(gender)
                        .and(ProfileSpecification.hasAgeGroup(ageGroup))
                        .and(ProfileSpecification.hasCountryId(countryId))
                        .and(ProfileSpecification.minAge(minAge))
                        .and(ProfileSpecification.maxAge(maxAge))
                        .and(ProfileSpecification.minGenderProbability(minGenderProb))
                        .and(ProfileSpecification.minCountryProbability(minCountryProb));

        String sortField = switch (sortBy == null ? "created_at" : sortBy) {
            case "age" -> "age";
            case "gender_probability" -> "genderProbability";
            default -> "createdAt";
        };

        Sort.Direction direction = "asc".equals(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortField));

        Page<Profile> result = profileRepository.findAll(specification, pageable);

        List<ProfileResponse> data = result.getContent()
                .stream()
                .map(ProfileResponse::from)
                .toList();

        return Map.of(
                "status", "success",
                "page", page,
                "limit", limit,
                "total", result.getTotalElements(),
                "data", data
        );
    }
}
