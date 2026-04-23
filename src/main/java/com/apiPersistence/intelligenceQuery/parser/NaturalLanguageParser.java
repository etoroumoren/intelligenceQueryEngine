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

        // Gender: if both present, do not constrain gender
        boolean hasMale = containsAnyWord(q, "male", "males", "man", "men");
        boolean hasFemale = containsAnyWord(q, "female", "females", "woman", "women");

        if (hasMale && !hasFemale) params.setGender("male");
        if (hasFemale && !hasMale) params.setGender("female");

        // Age group
        if (containsAnyWord(q, "child", "children")) {
            params.setAgeGroup("child");
        } else if (containsAnyWord(q, "teenager", "teenagers", "teen", "teens")) {
            params.setAgeGroup("teenager");
        } else if (containsAnyWord(q, "adult", "adults")) {
            params.setAgeGroup("adult");
        } else if (containsAnyWord(q, "senior", "seniors", "elderly")) {
            params.setAgeGroup("senior");
        }

        // young => 16..24
        if (containsAnyWord(q, "young")) {
            params.setMinAge(16);
            params.setMaxAge(24);
        }

        // above / over
        Matcher above = Pattern.compile("\\b(above|over|older than)\\s+(\\d+)\\b").matcher(q);
        if (above.find()) {
            params.setMinAge(Integer.parseInt(above.group(2)));
        }

        // below / under
        Matcher below = Pattern.compile("\\b(below|under|younger than)\\s+(\\d+)\\b").matcher(q);
        if (below.find()) {
            params.setMaxAge(Integer.parseInt(below.group(2)));
        }

        // Country mapping
        Map<String, String> countryMap = Map.ofEntries(
                Map.entry("nigeria", "NG"),
                Map.entry("kenya", "KE"),
                Map.entry("ghana", "GH"),
                Map.entry("angola", "AO"),
                Map.entry("benin", "BJ")
        );

        for (var e : countryMap.entrySet()) {
            if (q.contains(e.getKey())) {
                params.setCountryId(e.getValue());
                break;
            }
        }

        if (params.isEmpty()) {
            throw new UninterpretableQueryException();
        }

        return params;
    }

    private boolean containsAnyWord(String text, String... words) {
        for (String w : words) {
            if (Pattern.compile("\\b" + Pattern.quote(w) + "\\b").matcher(text).find()) return true;
        }
        return false;
    }
}