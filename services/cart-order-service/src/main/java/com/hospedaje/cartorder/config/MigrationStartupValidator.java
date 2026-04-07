package com.hospedaje.cartorder.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MigrationStartupValidator {

    private static final String HARDENING_SCRIPT = "V1__cart_items_quantity_hardening.sql";
    private static final MigrationVersion HARDENING_VERSION = MigrationVersion.fromVersion("1");

    private final Flyway flyway;

    @EventListener(ApplicationReadyEvent.class)
    public void validateHardeningMigration() {
        MigrationInfo[] applied = flyway.info().applied();
        boolean hardeningApplied = Arrays.stream(applied)
            .anyMatch(mi -> HARDENING_SCRIPT.equals(mi.getScript())
                || (mi.getVersion() != null && mi.getVersion().compareTo(HARDENING_VERSION) >= 0));

        if (!hardeningApplied) {
            throw new IllegalStateException(
                "No se encontró la migración crítica de hardening " + HARDENING_SCRIPT
                    + ". Aplique migraciones antes de exponer /cart/items."
            );
        }
    }
}
