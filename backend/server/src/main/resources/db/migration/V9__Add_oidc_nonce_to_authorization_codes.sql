ALTER TABLE oauth_authorization_codes
    ADD COLUMN nonce VARCHAR(255),
    ADD COLUMN scope VARCHAR(1000);
