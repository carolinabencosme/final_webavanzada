package com.hospedaje.cartorder.repository;

import com.hospedaje.cartorder.entity.CartItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.flyway.enabled=true"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemPostgresIntegrationTest {

    @SuppressWarnings("resource") // closed by Testcontainers / JUnit extension
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("orderdb_test")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void saveShouldPersistQuantityAsOneWhenNull() {
        CartItem item = CartItem.builder()
            .userId("tc-user")
            .propertyId("tc-prop")
            .propertyName("Casa Testcontainers")
            .city("Bogotá")
            .imageUrl("img")
            .checkIn(LocalDate.of(2026, 4, 10))
            .checkOut(LocalDate.of(2026, 4, 12))
            .quantity(null)
            .guests(2)
            .nights(2)
            .pricePerNight(new BigDecimal("80.00"))
            .lineTotal(new BigDecimal("160.00"))
            .build();

        CartItem persisted = cartItemRepository.saveAndFlush(item);
        CartItem reloaded = cartItemRepository.findById(persisted.getId()).orElseThrow();

        assertThat(reloaded.getQuantity()).isNotNull();
        assertThat(reloaded.getQuantity()).isEqualTo(1);
    }
}
