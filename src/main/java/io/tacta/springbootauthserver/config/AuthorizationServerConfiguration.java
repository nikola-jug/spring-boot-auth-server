package io.tacta.springbootauthserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.UUID;

@Configuration
@EnableConfigurationProperties(OAuthClientRegistrationProperties.class)
public class AuthorizationServerConfiguration {

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,
                                                                 OAuthClientRegistrationProperties properties) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        properties.getRegistrations().values().forEach(reg -> {
            if (repository.findByClientId(reg.getClientId()) == null) {
                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(reg.getClientId())
                        .clientSecret(reg.getClientSecret())
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUris(uris -> uris.addAll(reg.getRedirectUris()))
                        .postLogoutRedirectUris(uris -> uris.addAll(reg.getPostLogoutRedirectUris()))
                        .scopes(scopes -> scopes.addAll(reg.getScopes()))
                        .clientSettings(ClientSettings.builder()
                                .requireProofKey(reg.isRequireProofKey())
                                .requireAuthorizationConsent(reg.isRequireAuthorizationConsent())
                                .build())
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(reg.getAccessTokenTimeToLive())
                                .refreshTokenTimeToLive(reg.getRefreshTokenTimeToLive())
                                .authorizationCodeTimeToLive(reg.getAuthorizationCodeTimeToLive())
                                .build())
                        .build();
                repository.save(client);
            }
        });

        return repository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                           RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                         RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }
}
