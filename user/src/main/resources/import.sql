-- CREATE TABLE IF NOT EXISTS users (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     username        VARCHAR(100) UNIQUE NOT NULL,
--     email           VARCHAR(255) UNIQUE NOT NULL,
--     password_hash   TEXT NOT NULL,
--     roles            VARCHAR(20) NOT NULL, -- 'voter' or 'admin'
--     created_at      TIMESTAMP NOT NULL DEFAULT NOW()
-- );
-- NOTE: keep each INSERT as a single statement (semicolon-terminated)
-- and use the correct column names (schema uses `role`, not `roles`).
INSERT INTO users (username, email, password_hash, roles) VALUES ('admin', 'admin@example.com', 'admin', 'admin');
INSERT INTO users (username, email, password_hash, roles) VALUES ('user', 'user@example.com', 'user', 'voter');