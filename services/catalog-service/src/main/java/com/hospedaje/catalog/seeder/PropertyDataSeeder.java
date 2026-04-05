package com.hospedaje.catalog.seeder;

import com.hospedaje.catalog.document.Property;
import com.hospedaje.catalog.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyDataSeeder implements ApplicationRunner {

    private static final List<String> CITIES = List.of(
        "Santo Domingo", "Santiago", "Punta Cana", "Puerto Plata", "La Romana", "Samana", "Jarabacoa"
    );

    private static final List<String> PROPERTY_TYPES = List.of("HOTEL", "APARTMENT", "VILLA");
    private static final List<String> ROOM_TYPES = List.of("Estándar", "Doble", "Suite", "Estudio", "Deluxe", "Familiar");

    private final PropertyRepository propertyRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (propertyRepository.count() >= 20) {
            log.info("Property catalog already seeded (count>=20). Skipping.");
            return;
        }
        Faker faker = new Faker(new Locale("es"));
        int target = 42;
        int inserted = 0;
        for (int i = 0; i < target; i++) {
            String extRef = "PROP-" + UUID.randomUUID();
            if (propertyRepository.findByExternalRef(extRef).isPresent()) {
                continue;
            }
            String city = CITIES.get(faker.random().nextInt(CITIES.size()));
            String propertyType = PROPERTY_TYPES.get(faker.random().nextInt(PROPERTY_TYPES.size()));
            String roomType = ROOM_TYPES.get(faker.random().nextInt(ROOM_TYPES.size()));
            BigDecimal price = BigDecimal.valueOf(faker.number().randomDouble(2, 45, 420));
            int maxGuests = faker.random().nextInt(2, 8);
            String name = propertyLabel(propertyType, city, faker);
            List<String> amenities = List.of(
                faker.options().option("WiFi", "Piscina", "Gimnasio", "Spa", "Desayuno", "Estacionamiento", "Aire acondicionado", "Vista al mar"),
                faker.options().option("Cocina", "Lavadora", "TV", "Balcón", "Servicio a la habitación")
            );
            String img = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80&auto=format&fit=crop&sig=" + i;
            Property p = Property.builder()
                .name(name)
                .description(faker.lorem().paragraph(3))
                .city(city)
                .country("República Dominicana")
                .address(faker.address().streetAddress())
                .propertyType(propertyType)
                .roomType(roomType)
                .amenities(amenities)
                .imageUrl(img)
                .images(List.of(img))
                .pricePerNight(price)
                .maxGuests(maxGuests)
                .averageRating(Math.round(faker.random().nextDouble(3.5, 5.0) * 10.0) / 10.0)
                .totalReviews(faker.random().nextInt(0, 240))
                .externalRef(extRef)
                .build();
            propertyRepository.save(p);
            inserted++;
        }
        log.info("Property catalog seed completed. insertedApprox={}", inserted);
    }

    private static String propertyLabel(String propertyType, String city, Faker faker) {
        String brand = faker.company().name();
        return switch (propertyType) {
            case "HOTEL" -> "Hotel " + brand + " — " + city;
            case "APARTMENT" -> "Apartamento " + brand + " · " + city;
            default -> "Villa " + brand + " · " + city;
        };
    }
}
