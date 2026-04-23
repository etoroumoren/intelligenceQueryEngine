package com.apiPersistence.intelligenceQuery.seeding;

import com.apiPersistence.intelligenceQuery.entity.Profile;
import com.apiPersistence.intelligenceQuery.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.DeserializationFeature;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
        if (repository.count() > 0) {
            log.info("Database already contains data. Skipping seeding process.");
            return;
        }

        try (InputStream is = new ClassPathResource("profiles.json").getInputStream()) {
            JsonNode rootNode = mapper.readTree(is);

            JsonNode dataNode = rootNode.isArray()
                    ? rootNode
                    : rootNode.get("profiles");

            if (dataNode == null || !dataNode.isArray()) {
                throw new RuntimeException("Could not find a JSON array in profiles.json");
            }

            List<Profile> profiles = mapper.convertValue(
                    dataNode,
                    new TypeReference<List<Profile>>() {}
            );

            profiles.forEach(p -> {
                p.setId(null);
                p.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            });

            repository.saveAll(profiles);
            log.info("Successfully seeded {} profiles.", profiles.size());
        }
    }
}