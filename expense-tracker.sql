-- ===========================================
-- Expense Tracker Database Script
-- ===========================================

-- Create Database
CREATE DATABASE IF NOT EXISTS expense_tracker;

USE expense_tracker;

-- ===========================================
-- Users Table
-- ===========================================

CREATE TABLE IF NOT EXISTS users (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    email VARCHAR(100) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL

);

-- ===========================================
-- Expense Table
-- ===========================================

CREATE TABLE IF NOT EXISTS expense (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    title VARCHAR(255) NOT NULL,

    amount DOUBLE NOT NULL,

    category VARCHAR(100) NOT NULL,

    description TEXT,

    date DATE NOT NULL,

    receipt_path VARCHAR(255),

    user_id BIGINT NOT NULL,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE

);

-- ===========================================
-- Sample User
-- Password: password123 (Replace with BCrypt hash if needed)
-- ===========================================

INSERT INTO users (name, email, password)
VALUES
(
    'Demo User',
    'demo@example.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiM7jN7r5Y8vR6X1l9N5Y6o2M4X0R1K'
);

-- ===========================================
-- Sample Expenses
-- ===========================================

INSERT INTO expense
(title, amount, category, description, date, receipt_path, user_id)

VALUES

(
'Groceries',
1200,
'Food',
'Weekly grocery shopping',
'2026-07-01',
NULL,
1
),

(
'Petrol',
1800,
'Transport',
'Bike fuel',
'2026-07-03',
NULL,
1
),

(
'Electricity Bill',
2500,
'Bills',
'Monthly EB Bill',
'2026-07-05',
NULL,
1
),

(
'Movie',
500,
'Entertainment',
'Weekend movie',
'2026-07-10',
NULL,
1
),

(
'Restaurant',
900,
'Food',
'Dinner with friends',
'2026-07-12',
NULL,
1
);

-- ===========================================
-- Verify Data
-- ===========================================

SELECT * FROM users;

SELECT * FROM expense;