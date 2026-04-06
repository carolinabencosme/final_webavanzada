package com.hospedaje.cartorder.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MigrationStartupValidator {

    private static final String HARDENING_SCRIPT = "V1__cart_items_quantity_hardening.sql";

    private final Flyway flyway;

    @EventListener(ApplicationReadyEvent.class)
    public void validateHardeningMigration() {
        boolean hardeningApplied = Arrays.stream(flyway.info().applied())
            .map(MigrationInfo::getScript)
            .anyMatch(HARDENING_SCRIPT::equals);

        if (!hardeningApplied) {
            throw new IllegalStateException(
                "No se encontró la migración crítica de hardening " + HARDENING_SCRIPT
                    + ". Aplique migraciones antes de exponer /cart/items."
            );
        }
    }
}
