-- Migration script to add foire_id column to invitations table

-- Add foire_id column to invitations table
ALTER TABLE invitations ADD COLUMN foire_id BIGINT;

-- Add foreign key constraint
ALTER TABLE invitations 
ADD CONSTRAINT fk_invitation_foire 
FOREIGN KEY (foire_id) 
REFERENCES foires(id) 
ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_invitation_foire_id ON invitations(foire_id);
