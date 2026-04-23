package com.apiPersistence.intelligenceQuery.parser;


import lombok.Data;

@Data
public class FilterParams {

    private String gender;
    private String ageGroup;
    private String countryId;
    private Integer minAge;
    private Integer maxAge;

    public Boolean isEmpty() {
        return gender == null && ageGroup == null && countryId == null &&
                minAge == null && maxAge == null;
    }
}
