package io.tacta.springbootauthserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(OAuthClientRegistrationProperties.class)
public class AuthorizationServerConfiguration {

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,
                                                                 OAuthClientRegistrationProperties properties) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        properties.getRegistrations().values().forEach(reg -> {
            RegisteredClient existing = repository.findByClientId(reg.getClientId());
            String id = existing != null ? existing.getId() : UUID.randomUUID().toString();
            RegisteredClient client = RegisteredClient.withId(id)
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

    @Bean
    public JWKSource<SecurityContext> jwkSource(JdbcTemplate jdbcTemplate) {
        final String KEY_ID = "rsa-key-1";

        String existing = jdbcTemplate.query(
                "SELECT jwk_json FROM oauth2_jwk WHERE key_id = ?",
                rs -> rs.next() ? rs.getString("jwk_json") : null,
                KEY_ID);

        RSAKey rsaKey;
        if (existing != null) {
            try {
                rsaKey = RSAKey.parse(existing);
            } catch (ParseException e) {
                throw new IllegalStateException("Failed to parse persisted RSA JWK", e);
            }
        } else {
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                        .privateKey((RSAPrivateKey) keyPair.getPrivate())
                        .keyID(KEY_ID)
                        .build();
                jdbcTemplate.update(
                        "INSERT INTO oauth2_jwk (key_id, jwk_json) VALUES (?, ?)",
                        KEY_ID, rsaKey.toJSONString());
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("RSA algorithm unavailable", e);
            }
        }

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }
}
