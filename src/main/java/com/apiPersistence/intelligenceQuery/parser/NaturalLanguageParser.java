package com.apiPersistence.intelligenceQuery.parser;

import com.apiPersistence.intelligenceQuery.exception.UninterpretableQueryException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class NaturalLanguageParser {

    public FilterParams parse(String query) {
        if (query == null || query.isBlank()) {
            throw new UninterpretableQueryException();
        }

        String q = query.toLowerCase().trim();
        FilterParams params = new FilterParams();

        if (q.contains("female")) {
            params.setGender("female");
        } else if (q.contains("male")) {
            params.setGender("male");
        }

        // 2. Age Group Detection
        if (q.contains("child") || q.contains("children")) {
            params.setAgeGroup("child");
        } else if (q.contains("teenager")) {
            params.setAgeGroup("teenager");
        } else if (q.contains("adult")) {
            params.setAgeGroup("adult");
        } else if (q.contains("senior")) {
            params.setAgeGroup("senior");
        }

        if (q.contains("young")) {
            params.setMinAge(16);
            params.setMaxAge(24);
        }

        Pattern abovePattern = Pattern.compile("above (\\d+)");
        Matcher matcher = abovePattern.matcher(q);
        if (matcher.find()) {
            params.setMinAge(Integer.parseInt(matcher.group(1)));
        }

        // 5. Country Detection
        Map<String, String> countryMap = Map.of(
                "nigeria", "NG",
                "kenya", "KE",
                "ghana", "GH",
                "angola", "AO",
                "benin", "BJ"
        );

        for (Map.Entry<String, String> entry : countryMap.entrySet()) {
            if (q.contains(entry.getKey())) {
                params.setCountryId(entry.getValue());
                break;
            }
        }

        if (params.isEmpty()) {
            throw new UninterpretableQueryException();
        }

        return params;
    }
}