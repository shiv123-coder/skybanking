-- ================================================
-- Sky Banking System Database Schema (PostgreSQL)
-- ================================================

-- Drop existing schema if needed
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- FUNCTION for updating timestamp
CREATE OR REPLACE FUNCTION update_last_modified_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ==========================
-- Users table
-- ==========================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    fullname VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_phone ON users (phone);
CREATE INDEX idx_user_active ON users (is_active);
CREATE INDEX idx_user_created_at ON users (created_at);

CREATE TRIGGER update_users_modtime
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_last_modified_column();

-- ==========================
-- Accounts table
-- ==========================
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    account_type VARCHAR(20) CHECK (account_type IN ('SAVINGS', 'CURRENT', 'FIXED')) DEFAULT 'SAVINGS',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_transaction_date TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_account_user_id ON accounts (user_id);
CREATE INDEX idx_account_active ON accounts (is_active);
CREATE INDEX idx_account_type ON accounts (account_type);
CREATE INDEX idx_account_balance ON accounts (balance);

CREATE TRIGGER update_accounts_modtime
BEFORE UPDATE ON accounts
FOR EACH ROW EXECUTE FUNCTION update_last_modified_column();

-- ==========================
-- Transactions table
-- ==========================
CREATE TABLE transactions (
    txn_id SERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    txn_type VARCHAR(20) CHECK (txn_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'INTEREST', 'FEE')) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    tax_type VARCHAR(50) NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0.00,
    total_amount DECIMAL(15,2) NOT NULL,
    txn_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    receiver_account_id INT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')) DEFAULT 'COMPLETED',
    reference_number VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

CREATE INDEX idx_txn_account_id ON transactions (account_id);
CREATE INDEX idx_txn_type ON transactions (txn_type);
CREATE INDEX idx_txn_date ON transactions (txn_date);
CREATE INDEX idx_txn_status ON transactions (status);
CREATE INDEX idx_txn_reference_number ON transactions (reference_number);
CREATE INDEX idx_txn_receiver_account ON transactions (receiver_account_id);

CREATE TRIGGER update_transactions_modtime
BEFORE UPDATE ON transactions
FOR EACH ROW EXECUTE FUNCTION update_last_modified_column();

-- ==========================
-- Transaction Logs table
-- ==========================
CREATE TABLE transaction_logs (
    log_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT NOT NULL,
    transaction_type VARCHAR(20)  CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'INTEREST', 'FEE')) NOT NULL,
    amount VARCHAR(20) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

CREATE INDEX idx_txn_log_user_id ON transaction_logs (user_id);
CREATE INDEX idx_txn_log_account_id ON transaction_logs (account_id);
CREATE INDEX idx_txn_log_type ON transaction_logs (transaction_type);
CREATE INDEX idx_txn_log_status ON transaction_logs (status);
CREATE INDEX idx_txn_log_created_at ON transaction_logs (created_at);

-- ==========================
-- OTP Logs table
-- ==========================
CREATE TABLE otp_logs (
    otp_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    action VARCHAR(50) CHECK (action IN ('SIGNUP', 'LOGIN', 'PASSWORD_RESET', 'PROFILE_UPDATE')) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'FAILED')) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_otp_user_id ON otp_logs (user_id);
CREATE INDEX idx_otp_email ON otp_logs (email);
CREATE INDEX idx_otp_status ON otp_logs (status);
CREATE INDEX idx_otp_created_at ON otp_logs (created_at);
CREATE INDEX idx_otp_expires_at ON otp_logs (expires_at);

-- ==========================
-- Security Logs table
-- ==========================
CREATE TABLE security_logs (
    log_id SERIAL PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(50) CHECK (action IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PASSWORD_CHANGE', 'PROFILE_UPDATE', 
                                         'SUSPICIOUS_ACTIVITY', 'ADMIN_LOGIN', 'ADMIN_LOGIN_FAILED')) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_sec_log_user_id ON security_logs (user_id);
CREATE INDEX idx_sec_log_action ON security_logs (action);
CREATE INDEX idx_sec_log_ip_address ON security_logs (ip_address);
CREATE INDEX idx_sec_log_created_at ON security_logs (created_at);

-- ==========================
-- Admins table
-- ==========================
CREATE TABLE admins (
    admin_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admin_username ON admins (username);
CREATE INDEX idx_admin_email ON admins (email);
CREATE INDEX idx_admin_active ON admins (is_active);

CREATE TRIGGER update_admins_modtime
BEFORE UPDATE ON admins
FOR EACH ROW EXECUTE FUNCTION update_last_modified_column();

-- ==========================
-- Admin Logs table
-- ==========================
CREATE TABLE admin_logs (
    log_id SERIAL PRIMARY KEY,
    admin_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_user INT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE CASCADE,
    FOREIGN KEY (target_user) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_admin_log_admin_id ON admin_logs (admin_id);
CREATE INDEX idx_admin_log_action ON admin_logs (action);
CREATE INDEX idx_admin_log_target_user ON admin_logs (target_user);
CREATE INDEX idx_admin_log_created_at ON admin_logs (created_at);

-- ==========================
-- System Settings table
-- ==========================
CREATE TABLE system_settings (
    setting_id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_setting_key ON system_settings (setting_key);

CREATE TRIGGER update_settings_modtime
BEFORE UPDATE ON system_settings
FOR EACH ROW EXECUTE FUNCTION update_last_modified_column();

-- ==========================
-- Notifications table
-- ==========================
CREATE TABLE notifications (
    notification_id SERIAL PRIMARY KEY,
    user_id INT NULL,
    admin_id INT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) CHECK (type IN ('INFO', 'WARNING', 'ERROR', 'SUCCESS')) DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_user_id ON notifications (user_id);
CREATE INDEX idx_notif_admin_id ON notifications (admin_id);
CREATE INDEX idx_notif_type ON notifications (type);
CREATE INDEX idx_notif_is_read ON notifications (is_read);
CREATE INDEX idx_notif_created_at ON notifications (created_at);

-- ==========================
-- Procedures / Functions
-- ==========================

CREATE OR REPLACE FUNCTION CreateUserWithAccount(
    p_fullname VARCHAR,
    p_username VARCHAR,
    p_email VARCHAR,
    p_phone VARCHAR,
    p_password_hash VARCHAR,
    p_account_type VARCHAR
) RETURNS TABLE (user_id INT, account_id INT) AS $$
DECLARE
    v_user_id INT;
    v_account_id INT;
BEGIN
    INSERT INTO users (fullname, username, email, phone, password_hash)
    VALUES (p_fullname, p_username, p_email, p_phone, p_password_hash)
    RETURNING users.user_id INTO v_user_id;

    INSERT INTO accounts (accounts.user_id, account_type) 
    VALUES (v_user_id, p_account_type)
    RETURNING accounts.account_id INTO v_account_id;

    RETURN QUERY SELECT v_user_id, v_account_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ProcessTransaction(
    p_account_id INT,
    p_txn_type VARCHAR,
    p_amount DECIMAL,
    p_tax_amount DECIMAL,
    p_total_amount DECIMAL,
    p_description TEXT,
    p_receiver_account_id INT DEFAULT NULL
) RETURNS TABLE (txn_id INT, reference_number VARCHAR) AS $$
DECLARE
    v_current_balance DECIMAL;
    v_reference_number VARCHAR;
    v_txn_id INT;
BEGIN
    SELECT balance INTO v_current_balance FROM accounts WHERE account_id = p_account_id;

    IF p_txn_type IN ('WITHDRAWAL', 'TRANSFER') AND v_current_balance < p_total_amount THEN
        RAISE EXCEPTION 'Insufficient balance';
    END IF;

    v_reference_number := 'TXN' || EXTRACT(EPOCH FROM NOW())::BIGINT || LPAD(p_account_id::TEXT, 6, '0');

    INSERT INTO transactions (account_id, txn_type, amount, tax_amount, total_amount, 
                              description, receiver_account_id, reference_number)
    VALUES (p_account_id, p_txn_type, p_amount, p_tax_amount, p_total_amount, 
            p_description, p_receiver_account_id, v_reference_number)
    RETURNING transactions.txn_id INTO v_txn_id;

    IF p_txn_type = 'DEPOSIT' THEN
        UPDATE accounts SET balance = balance + p_total_amount, last_transaction_date = NOW()
        WHERE account_id = p_account_id;
    ELSE
        UPDATE accounts SET balance = balance - p_total_amount, last_transaction_date = NOW()
        WHERE account_id = p_account_id;
    END IF;

    IF p_txn_type = 'TRANSFER' AND p_receiver_account_id IS NOT NULL THEN
        UPDATE accounts SET balance = balance + p_amount, last_transaction_date = NOW()
        WHERE account_id = p_receiver_account_id;
    END IF;

    RETURN QUERY SELECT v_txn_id, v_reference_number;
END;
$$ LANGUAGE plpgsql;

-- ==========================
-- Triggers
-- ==========================

CREATE OR REPLACE FUNCTION log_user_update() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.password_hash != NEW.password_hash THEN
        INSERT INTO security_logs (user_id, action, ip_address, details)
        VALUES (NEW.user_id, 'PASSWORD_CHANGE', 'SYSTEM', 'Password updated');
    END IF;
    IF OLD.is_active != NEW.is_active THEN
        INSERT INTO security_logs (user_id, action, ip_address, details)
        VALUES (NEW.user_id, 'PROFILE_UPDATE', 'SYSTEM', 
                'Account status changed to ' || CASE WHEN NEW.is_active THEN 'ACTIVE' ELSE 'INACTIVE' END);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER user_update_audit
AFTER UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION log_user_update();


CREATE OR REPLACE FUNCTION log_transaction_create() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO transaction_logs (user_id, account_id, transaction_type, amount, status, description)
    VALUES (
        (SELECT user_id FROM accounts WHERE account_id = NEW.account_id),
        NEW.account_id,
        NEW.txn_type,
        NEW.total_amount::CHAR(20),
        'SUCCESS',
        NEW.description
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transaction_create_audit
AFTER INSERT ON transactions
FOR EACH ROW EXECUTE FUNCTION log_transaction_create();
