CREATE TABLE oauth2_jwk (
    key_id     VARCHAR(100) PRIMARY KEY,
    jwk_json   TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
