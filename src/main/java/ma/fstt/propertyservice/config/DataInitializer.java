package ma.fstt.propertyservice.config;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.model.PropertyType;
import ma.fstt.propertyservice.repository.PropertyTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PropertyTypeRepository propertyTypeRepository;

    private static final List<String> DEFAULT_PROPERTY_TYPES = Arrays.asList(
            "Apartment",
            "House",
            "Studio",
            "Condo",
            "Villa"
    );

    @Override
    public void run(String... args) throws Exception {
        int createdCount = 0;
        int existingCount = 0;

        for (String typeName : DEFAULT_PROPERTY_TYPES) {
            Optional<PropertyType> existingType = propertyTypeRepository.findByTypeIgnoreCase(typeName);

            if (existingType.isEmpty()) {
                PropertyType propertyType = new PropertyType();
                propertyType.setType(typeName);
                propertyTypeRepository.save(propertyType);
                createdCount++;
            } else {
                existingCount++;
            }
        }
    }
}

