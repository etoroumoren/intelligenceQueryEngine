package com.apiPersistence.intelligenceQuery.repository;

import com.apiPersistence.intelligenceQuery.entity.Profile;
import org.springframework.data.jpa.domain.Specification;


public class ProfileSpecification {

    public static Specification<Profile> hasGender(String gender) {
        return (root, query, cb) -> gender == null
                ? cb.conjunction()
                : cb.equal(root.get("gender"), gender);
    }

    public static Specification<Profile> hasAgeGroup(String ageGroup) {
        return (root, query, cb) -> ageGroup == null
                ? cb.conjunction()
                : cb.equal(root.get("ageGroup"), ageGroup);
    }

    public static Specification<Profile> hasCountryId(String countryId) {
        return (root, query, cb) -> countryId == null
                ? cb.conjunction()
                : cb.equal(root.get("countryId"), countryId);
    }

    public static Specification<Profile> minAge(Integer min) {
        return (root, query, cb) -> min == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("age"), min);
    }

    public static Specification<Profile> maxAge(Integer max) {
        return (root, query, cb) -> max == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("age"), max);
    }

    public static Specification<Profile> minGenderProbability(Double min) {
        return (root, query, cb) -> min == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("genderProbability"), min);
    }

    public static Specification<Profile> minCountryProbability(Double min) {
        return (root, query, cb) -> min == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("countryProbability"), min);
    }

}