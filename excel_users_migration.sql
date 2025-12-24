-- Migration script to update excel_users table for PostgreSQL
-- Allow foire_id to be NULL to prevent foreign key constraint violations when deleting foires

-- Step 1: Update existing NOT NULL constraint to allow NULL
ALTER TABLE excel_users ALTER COLUMN foire_id DROP NOT NULL;

-- Verification query (run this separately to check the result)
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'excel_users' 
-- ORDER BY ordinal_position;
