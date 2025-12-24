-- Fix database schema to match Foire entity
-- Run this SQL script in your PostgreSQL database

-- First, check if country_id column exists and drop it if it does
ALTER TABLE foires DROP COLUMN IF EXISTS country_id;

-- Ensure country_code column exists with correct type
-- If it doesn't exist, this will create it
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'foires' AND column_name = 'country_code'
    ) THEN
        ALTER TABLE foires ADD COLUMN country_code VARCHAR(2);
    END IF;
END $$;

-- Make sure is_active has a default value
ALTER TABLE foires ALTER COLUMN is_active SET DEFAULT false;

-- Update any existing null values in is_active
UPDATE foires SET is_active = false WHERE is_active IS NULL;

-- Ensure created_at and updated_at columns exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'foires' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE foires ADD COLUMN created_at TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'foires' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE foires ADD COLUMN updated_at TIMESTAMP;
    END IF;
END $$;

-- Set default timestamps for existing records
UPDATE foires SET created_at = NOW() WHERE created_at IS NULL;
UPDATE foires SET updated_at = NOW() WHERE updated_at IS NULL;

-- Verify the schema
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'foires' 
ORDER BY ordinal_position;
