package io.tacta.springbootauthserver.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizationCleanup {

    private final JdbcTemplate jdbcTemplate;

    public OAuth2AuthorizationCleanup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = 300_000)
    public void purgeExpiredAuthorizations() {
        jdbcTemplate.update("""
                DELETE FROM oauth2_authorization
                WHERE (refresh_token_expires_at IS NOT NULL AND refresh_token_expires_at < NOW())
                   OR (refresh_token_expires_at IS NULL AND access_token_expires_at IS NOT NULL AND access_token_expires_at < NOW())
                """);
    }
}
