package io.tacta.springbootauthserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "oauth.client")
public class OAuthClientRegistrationProperties {

    private Map<String, RegistrationProperties> registrations = new LinkedHashMap<>();

    @Data
    public static class RegistrationProperties {
        private String clientId;
        private String clientSecret;
        private List<String> redirectUris;
        private List<String> postLogoutRedirectUris;
        private List<String> scopes;
        private Duration accessTokenTimeToLive = Duration.ofMinutes(5);
        private Duration refreshTokenTimeToLive = Duration.ofHours(24);
        private Duration authorizationCodeTimeToLive = Duration.ofMinutes(5);
        private boolean requireProofKey = true;
        private boolean requireAuthorizationConsent = false;
    }
}
