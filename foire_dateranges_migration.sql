-- Migration script to update Foire table for PostgreSQL
-- Add support for multiple date ranges, location field, and fix description field

-- Step 1: Add new columns
ALTER TABLE foires ADD COLUMN IF NOT EXISTS date_ranges TEXT;
ALTER TABLE foires ADD COLUMN IF NOT EXISTS location VARCHAR(255);

-- Step 2: Rename Description to description (lowercase) if needed
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'foires' 
        AND column_name = 'Description'
    ) THEN
        ALTER TABLE foires RENAME COLUMN "Description" TO description;
    END IF;
END $$;

-- Step 3: Ensure description column is TEXT type
ALTER TABLE foires ALTER COLUMN description TYPE TEXT;

-- Step 4: Migrate existing single date to date_ranges JSON format (optional - for backward compatibility)
-- This will convert existing date/endDate to the first date range in the JSON array
UPDATE foires 
SET date_ranges = json_build_array(
    json_build_object(
        'startDate', TO_CHAR(date, 'YYYY-MM-DD'),
        'endDate', COALESCE(TO_CHAR(end_date, 'YYYY-MM-DD'), TO_CHAR(date, 'YYYY-MM-DD'))
    )
)::text
WHERE date IS NOT NULL AND (date_ranges IS NULL OR date_ranges = '');

-- Verification query (run this separately to check the result)
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'foires' 
-- ORDER BY ordinal_position;
