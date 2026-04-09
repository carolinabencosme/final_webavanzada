-- Modelo de inventario por unidad para evitar sobreventa.

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS room_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS room_unit_id VARCHAR(255);

ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS room_type VARCHAR(100);

UPDATE reservations
SET room_type = COALESCE(NULLIF(TRIM(room_type), ''), 'STANDARD')
WHERE room_type IS NULL OR TRIM(room_type) = '';

UPDATE cart_items
SET room_type = COALESCE(NULLIF(TRIM(room_type), ''), 'STANDARD')
WHERE room_type IS NULL OR TRIM(room_type) = '';

CREATE TABLE IF NOT EXISTS room_units
(
    id          BIGSERIAL PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL,
    room_type   VARCHAR(100) NOT NULL,
    unit_code   VARCHAR(255) NOT NULL,
    unit_order  INTEGER      NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_room_unit_code
    ON room_units (unit_code);

CREATE UNIQUE INDEX IF NOT EXISTS uk_room_unit_property_type_order
    ON room_units (property_id, room_type, unit_order);

CREATE INDEX IF NOT EXISTS idx_room_unit_property_type
    ON room_units (property_id, room_type);

DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN (
        SELECT DISTINCT r.property_id,
                        COALESCE(NULLIF(TRIM(r.room_type), ''), 'STANDARD') AS room_type
        FROM reservations r
        WHERE r.property_id IS NOT NULL
    )
    LOOP
        INSERT INTO room_units (property_id, room_type, unit_code, unit_order)
        VALUES (rec.property_id, rec.room_type, rec.property_id || '::' || rec.room_type || '::1', 1)
        ON CONFLICT (property_id, room_type, unit_order) DO NOTHING;
    END LOOP;
END $$;

