package com.apiPersistence.intelligenceQuery.seeding;

import com.apiPersistence.intelligenceQuery.entity.Profile;
import com.apiPersistence.intelligenceQuery.repository.ProfileRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProfileRepository repository;
    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(ProfileRepository repository){
        this.repository = repository;
        this.mapper = new ObjectMapper();
        this.mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("Database already contains data. Skipping seeding process.");
            return;
        }

        try (InputStream is = new ClassPathResource("profiles.json").getInputStream()) {
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(is);

            com.fasterxml.jackson.databind.JsonNode dataNode = rootNode.isArray()
                    ? rootNode
                    : rootNode.get("profiles"); // Adjust "profiles" to "data" or whatever the key is

            if (dataNode == null || !dataNode.isArray()) {
                throw new RuntimeException("Could not find a JSON array in profiles.json");
            }

            // 3. Convert that specific node into your List
            List<Profile> profiles = mapper.convertValue(dataNode, new TypeReference<List<Profile>>() {});

            profiles.forEach(p -> {
                p.setId(null);
                p.setCreatedAt(OffsetDateTime.now(java.time.ZoneOffset.UTC));
            });

            repository.saveAll(profiles);
            System.out.println("Successfully seeded " + profiles.size() + " profiles.");
        }
    }
}
