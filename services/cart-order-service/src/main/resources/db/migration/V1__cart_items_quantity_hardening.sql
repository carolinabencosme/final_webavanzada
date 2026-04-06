DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'cart_items'
    ) THEN
        UPDATE cart_items
        SET quantity = 1
        WHERE quantity IS NULL;

        ALTER TABLE cart_items
            ALTER COLUMN quantity SET DEFAULT 1;

        ALTER TABLE cart_items
            ALTER COLUMN quantity SET NOT NULL;
    END IF;
END $$;
