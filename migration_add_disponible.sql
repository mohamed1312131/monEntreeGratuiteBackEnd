-- Migration: Add disponible column to foires table
-- Date: 2025-12-29
-- Description: Adds a new 'disponible' boolean column to track foire availability status

-- Add the disponible column with default value true
ALTER TABLE foires 
ADD COLUMN IF NOT EXISTS disponible BOOLEAN NOT NULL DEFAULT true;

-- Update existing records to have disponible = true
UPDATE foires 
SET disponible = true 
WHERE disponible IS NULL;

-- Verify the column was added
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'foires' AND column_name = 'disponible';
