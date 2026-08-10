-- Add submitter IP address tracking for reservations.
-- Existing reservations remain valid and will display N/A in the admin table.
ALTER TABLE reservations
ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);
