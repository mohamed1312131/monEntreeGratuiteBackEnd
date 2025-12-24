-- Add video_url column to about_us table
-- Run this SQL script in your PostgreSQL database

-- Add video_url column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'about_us' AND column_name = 'video_url'
    ) THEN
        ALTER TABLE about_us ADD COLUMN video_url VARCHAR(500);
    END IF;
END $$;

-- Verify the schema
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'about_us' 
ORDER BY ordinal_position;
