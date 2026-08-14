-- Add auditable consent fields to reservations.
-- Existing reservations intentionally remain NULL so the admin can show "Non renseigné".
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS phone_contact_consent BOOLEAN,
    ADD COLUMN IF NOT EXISTS partner_data_sharing_consent BOOLEAN,
    ADD COLUMN IF NOT EXISTS marketing_consent BOOLEAN,
    ADD COLUMN IF NOT EXISTS terms_accepted BOOLEAN,
    ADD COLUMN IF NOT EXISTS consent_captured_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS terms_version VARCHAR(32),
    ADD COLUMN IF NOT EXISTS privacy_policy_version VARCHAR(32);
