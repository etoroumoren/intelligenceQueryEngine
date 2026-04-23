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

        if(limit > 50) {
            limit = 50;
        }

        if(limit < 1) {
            limit = 10;
        }
        Specification<Profile> specification =
                ProfileSpecification.hasGender(gender)
                        .and(ProfileSpecification.hasAgeGroup(ageGroup))
                        .and(ProfileSpecification.hasCountryId(countryId))
                        .and(ProfileSpecification.minAge(minAge))
                        .and(ProfileSpecification.maxAge(maxAge))
                        .and(ProfileSpecification.minGenderProbability(minGenderProb))
                        .and(ProfileSpecification.minCountryProbability(minCountryProb));

        String sortField = switch (sortBy == null ? "" : sortBy){
            case "age" -> "age";
            case "gender_probability" -> "genderProbability";
            case "created_at"         -> "createdAt";
            default                   -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortField));

        Page<Profile> result = profileRepository.findAll(specification, pageable);

        List<ProfileResponse> data = result.getContent()
                .stream()
                .map(ProfileResponse::from)
                .collect(Collectors.toList());


        return Map.of(
                "status", "success",
                "page", page,
                "limit", limit,
                "total", result.getTotalElements(),
                "data", data
        );

    }
}
