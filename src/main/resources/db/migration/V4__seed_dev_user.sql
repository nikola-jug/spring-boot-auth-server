INSERT INTO users (username, password, email, first_name, last_name)
VALUES ('user', '{noop}password', 'user@spring-boot-auth-server.local', 'User', 'User');

INSERT INTO authorities (username, authority)
VALUES ('user', 'ROLE_USER');
