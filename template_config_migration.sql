-- Migration to add templateConfig column to email_templates table
-- Run this SQL script in your database

ALTER TABLE email_templates 
ADD COLUMN template_config TEXT;

-- Set default empty config for existing templates
UPDATE email_templates 
SET template_config = '{}' 
WHERE template_config IS NULL;

-- Verify the change
SELECT id, name, 
       CASE 
           WHEN template_config IS NULL THEN 'NULL'
           WHEN template_config = '' THEN 'EMPTY'
           ELSE 'HAS_VALUE'
       END as config_status
FROM email_templates;
