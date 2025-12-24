-- Migration script to update Slider table for PostgreSQL
-- Remove name, description, and foire_id columns
-- Keep image_url, reference, slider_order, and is_active

-- Step 1: Remove foreign key constraint if it exists
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fkslider_foire' 
        AND table_name = 'slider'
    ) THEN
        ALTER TABLE slider DROP CONSTRAINT fkslider_foire;
    END IF;
END $$;

-- Step 2: Drop the columns we no longer need
ALTER TABLE slider DROP COLUMN IF EXISTS name;
ALTER TABLE slider DROP COLUMN IF EXISTS description;
ALTER TABLE slider DROP COLUMN IF EXISTS foire_id;

-- Step 3: Ensure is_active column is NOT NULL with default value
ALTER TABLE slider ALTER COLUMN is_active SET NOT NULL;
ALTER TABLE slider ALTER COLUMN is_active SET DEFAULT false;

-- Step 4: Update any existing NULL values to false before setting NOT NULL
UPDATE slider SET is_active = false WHERE is_active IS NULL;

-- Verification query (run this separately to check the result)
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'slider' 
-- ORDER BY ordinal_position;
