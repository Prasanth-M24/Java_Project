DROP DATABASE IF EXISTS bank_management;
CREATE DATABASE bank_management;
USE bank_management;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_users
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255),
    target_account_id INT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transactions_target_account
        FOREIGN KEY (target_account_id) REFERENCES accounts(account_id)
        ON DELETE SET NULL
);

INSERT INTO users (full_name, email, phone, password) VALUES
('Rahul Sharma', 'rahul@example.com', '9876543210', 'pass123'),
('Anita Verma', 'anita@example.com', '9123456780', 'pass123');

INSERT INTO accounts (user_id, account_number, account_type, balance, status) VALUES
(1, '1012345678', 'SAVINGS', 14000.00, 'ACTIVE'),
(2, '1098765432', 'CURRENT', 23000.00, 'ACTIVE');

INSERT INTO transactions (account_id, transaction_type, amount, description, target_account_id) VALUES
(1, 'DEPOSIT', 15000.00, 'Opening deposit', NULL),
(2, 'DEPOSIT', 22000.00, 'Opening deposit', NULL),
(1, 'TRANSFER_OUT', 1000.00, 'Transfer to 1098765432', 2),
(2, 'TRANSFER_IN', 1000.00, 'Transfer from 1012345678', 1);
