-- Clear existing data
DELETE FROM pension_applications;
DELETE FROM users;

-- Insert Admin Users
INSERT INTO users (id, full_name, email, password, phone_number, role, employee_id, department, active, email_verified, created_at, updated_at)
VALUES 
(1, 'Admin Kumar', 'admin1@epension.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '9876543210', 'ADMIN', 'ADM001', 'Pension Management', true, true, NOW(), NOW()),
(2, 'Supervisor Reddy', 'supervisor@epension.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '9876543211', 'ADMIN', 'ADM002', 'Verification', true, true, NOW(), NOW());

-- Insert Your User Account
INSERT INTO users (id, full_name, email, password, phone_number, role, aadhar_number, date_of_birth, retirement_date, pension_type, active, email_verified, created_at, updated_at)
VALUES 
(3, 'Abhinaya', 'abhinaya@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '6309692504', 'PENSIONER', '123456789012', '2000-01-01', '2060-01-01', 'RETIREMENT', true, true, NOW(), NOW());

-- Insert Regular Pensioner Users
INSERT INTO users (id, full_name, email, password, phone_number, role, aadhar_number, date_of_birth, retirement_date, pension_type, active, email_verified, created_at, updated_at)
VALUES 
(4, 'Ramesh Kumar', 'ramesh@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '9876543212', 'PENSIONER', '123456789013', '1960-05-15', '2020-05-31', 'RETIREMENT', true, true, NOW(), NOW()),
(5, 'Suresh Reddy', 'suresh@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '9876543213', 'PENSIONER', '123456789014', '1962-08-20', '2022-08-31', 'RETIREMENT', true, true, NOW(), NOW()),
(6, 'Lakshmi Devi', 'lakshmi@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '9876543214', 'PENSIONER', '123456789015', '1965-03-10', '2023-03-31', 'FAMILY', true, true, NOW(), NOW());
