CREATE TABLE users (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username                VARCHAR(50)  NOT NULL UNIQUE,
    password                VARCHAR(500) NOT NULL,
    email                   VARCHAR(200) NOT NULL UNIQUE,
    first_name              VARCHAR(100),
    last_name               VARCHAR(100),
    enabled                 BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_expired     BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked      BOOLEAN      NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE authorities (
    username  VARCHAR(50) NOT NULL REFERENCES users (username),
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT ix_auth_username UNIQUE (username, authority)
);
