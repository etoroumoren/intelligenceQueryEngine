package com.apiPersistence.intelligenceQuery.controller;


import com.apiPersistence.intelligenceQuery.parser.FilterParams;
import com.apiPersistence.intelligenceQuery.parser.NaturalLanguageParser;
import com.apiPersistence.intelligenceQuery.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;
    private final NaturalLanguageParser parser;

    public ProfileController(ProfileService profileService, NaturalLanguageParser parser){
        this.profileService = profileService;
        this.parser = parser;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProfiles(
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name="age_group", required=false) String ageGroup,
            @RequestParam(name="country_id", required=false) String countryId,
            @RequestParam(name="min_age", required=false) Integer minAge,
            @RequestParam(name="max_age", required=false) Integer maxAge,
            @RequestParam(name="min_gender_probability", required=false) Double minGenderProbability,
            @RequestParam(name="min_country_probability", required=false) Double minCountryProbability,
            @RequestParam(name="sort_by", defaultValue="created_at") String sortBy,
            @RequestParam(name="order", defaultValue="asc") String order,
            @RequestParam(name="page", defaultValue="1") int page,
            @RequestParam(name="limit", defaultValue="10") int limit
    ) {
        return ResponseEntity.ok(
                profileService.getProfiles(
                        gender, ageGroup, countryId, minAge,maxAge,
                        minGenderProbability, minCountryProbability,
                        sortBy, order, page, limit
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProfiles(
            @RequestParam(name = "q") String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Missing or empty parameter"
            ));
        }

        FilterParams params = parser.parse(query);

        Map<String, Object> response = profileService.getProfiles(
                params.getGender(),
                params.getAgeGroup(),
                params.getCountryId(),
                params.getMinAge(),
                params.getMaxAge(),
                null, null,
                "created_at", "desc", page, limit
        );

        return ResponseEntity.ok(response);
    }
}
