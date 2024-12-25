-- Update existing users to have ROLE_ prefix
UPDATE users 
SET role = CONCAT('ROLE_', role) 
WHERE role NOT LIKE 'ROLE_%';
