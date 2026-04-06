package com.hospedaje.cartorder.repository;

import com.hospedaje.cartorder.entity.CartItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=false",
    "logging.level.org.hibernate.SQL=DEBUG",
    "logging.level.org.hibernate.orm.jdbc.bind=TRACE"
})
@ExtendWith(OutputCaptureExtension.class)
class CartItemInsertSqlTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void insertSqlShouldIncludeQuantityColumn(CapturedOutput output) {
        CartItem item = CartItem.builder()
            .userId("sql-user")
            .propertyId("prop-1")
            .propertyName("Propiedad SQL")
            .city("Madrid")
            .imageUrl("img")
            .checkIn(LocalDate.of(2026, 4, 8))
            .checkOut(LocalDate.of(2026, 4, 10))
            .quantity(1)
            .guests(2)
            .nights(2)
            .pricePerNight(new BigDecimal("120.00"))
            .lineTotal(new BigDecimal("240.00"))
            .build();

        cartItemRepository.saveAndFlush(item);

        String logs = output.getOut().toLowerCase(Locale.ROOT);
        assertThat(logs).contains("insert into cart_items");
        assertThat(logs).contains("quantity");
    }
}
