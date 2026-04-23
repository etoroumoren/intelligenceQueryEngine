package com.apiPersistence.intelligenceQuery.seeding;

import com.apiPersistence.intelligenceQuery.entity.Profile;
import com.apiPersistence.intelligenceQuery.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProfileRepository repository;
    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(ProfileRepository repository) {
        this.repository = repository;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        try (InputStream is = new ClassPathResource("profiles.json").getInputStream()) {
            JsonNode root = mapper.readTree(is);
            JsonNode dataNode = root.isArray() ? root : root.get("profiles");

            if (dataNode == null || !dataNode.isArray()) {
                throw new RuntimeException("profiles.json must contain an array (root or 'profiles').");
            }

            List<Profile> toSave = new ArrayList<>();

            for (JsonNode n : dataNode) {
                String name = text(n, "name");
                if (name == null || name.isBlank()) continue;

                Optional<Profile> existing = repository.findByName(name);
                Profile p = existing.orElseGet(Profile::new);

                p.setName(name);
                p.setGender(text(n, "gender"));
                p.setGenderProbability(number(n, "gender_probability"));
                p.setAge(intNumber(n, "age"));
                p.setAgeGroup(text(n, "age_group"));
                p.setCountryId(text(n, "country_id"));
                p.setCountryName(text(n, "country_name"));
                p.setCountryProbability(number(n, "country_probability"));

                if (p.getCreatedAt() == null) {
                    p.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                }

                toSave.add(p);
            }

            repository.saveAll(toSave);
            log.info("Seed upsert completed. processed={}", toSave.size());
        }
    }

    private String text(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private Double number(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asDouble();
    }

    private Integer intNumber(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asInt();
    }
}