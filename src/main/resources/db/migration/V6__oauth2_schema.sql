-- Lưu ý: Nếu bảng đã tồn tại, hãy DROP đi để Flyway chạy lại bản sạch
DROP TABLE IF EXISTS oauth2_authorization;

CREATE TABLE oauth2_authorization (
                                      id varchar(100) NOT NULL,
                                      registered_client_id varchar(100) NOT NULL,
                                      principal_name varchar(200) NOT NULL,
                                      authorization_grant_type varchar(100) NOT NULL,
                                      authorized_scopes varchar(1000) DEFAULT NULL,
                                      attributes text DEFAULT NULL, -- Dùng TEXT cho Postgres
                                      state varchar(500) DEFAULT NULL,
                                      authorization_code_value text DEFAULT NULL,
                                      authorization_code_issued_at timestamp NULL DEFAULT NULL,
                                      authorization_code_expires_at timestamp NULL DEFAULT NULL,
                                      authorization_code_metadata text DEFAULT NULL,
                                      access_token_value text DEFAULT NULL,
                                      access_token_issued_at timestamp NULL DEFAULT NULL,
                                      access_token_expires_at timestamp NULL DEFAULT NULL,
                                      access_token_metadata text DEFAULT NULL,
                                      access_token_type varchar(100) DEFAULT NULL,
                                      access_token_scopes varchar(1000) DEFAULT NULL,
                                      oidc_id_token_value text DEFAULT NULL,
                                      oidc_id_token_issued_at timestamp NULL DEFAULT NULL,
                                      oidc_id_token_expires_at timestamp NULL DEFAULT NULL,
                                      oidc_id_token_metadata text DEFAULT NULL,
                                      refresh_token_value text DEFAULT NULL,
                                      refresh_token_issued_at timestamp NULL DEFAULT NULL,
                                      refresh_token_expires_at timestamp NULL DEFAULT NULL,
                                      refresh_token_metadata text DEFAULT NULL,
                                      user_code_value text DEFAULT NULL,
                                      user_code_issued_at timestamp NULL DEFAULT NULL,
                                      user_code_expires_at timestamp NULL DEFAULT NULL,
                                      user_code_metadata text DEFAULT NULL,
                                      device_code_value text DEFAULT NULL,
                                      device_code_issued_at timestamp NULL DEFAULT NULL,
                                      device_code_expires_at timestamp NULL DEFAULT NULL,
                                      device_code_metadata text DEFAULT NULL,
                                      PRIMARY KEY (id)
);