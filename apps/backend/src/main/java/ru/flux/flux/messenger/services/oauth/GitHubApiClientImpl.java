package ru.flux.flux.messenger.services.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import ru.flux.flux.messenger.exceptions.OAuthVerificationException;

import java.util.List;
import java.util.Map;

@Component
public class GitHubApiClientImpl implements GitHubApiClient {

    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_URL = "https://api.github.com/user";
    private static final String EMAILS_URL = "https://api.github.com/user/emails";

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public GitHubApiClientImpl(@Value("${oauth.github.client-id:}") String clientId,
                               @Value("${oauth.github.client-secret:}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    @Override
    public OAuthUserInfo exchange(String code, String codeVerifier, String redirectUri) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new OAuthVerificationException("GitHub OAuth client is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            form.add("code_verifier", codeVerifier);
        }

        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(TOKEN_URL)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new OAuthVerificationException("GitHub token exchange failed", e);
        }

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            String err = tokenResponse != null ? String.valueOf(tokenResponse.get("error_description")) : "no response";
            throw new OAuthVerificationException("GitHub did not return an access token: " + err);
        }

        String accessToken = String.valueOf(tokenResponse.get("access_token"));

        Map<String, Object> userJson;
        try {
            userJson = restClient.get()
                    .uri(USER_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new OAuthVerificationException("Failed to load GitHub user profile", e);
        }
        if (userJson == null || userJson.get("id") == null) {
            throw new OAuthVerificationException("GitHub did not return a user profile");
        }

        String providerUserId = String.valueOf(userJson.get("id"));
        String email = asString(userJson.get("email"));
        String name = asString(userJson.get("name"));
        String avatarUrl = asString(userJson.get("avatar_url"));
        String login = asString(userJson.get("login"));

        if (email == null || email.isBlank()) {
            email = fetchPrimaryEmail(accessToken);
        }

        String firstName = null;
        String lastName = null;
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : null;
        } else if (login != null) {
            firstName = login;
        }

        return new OAuthUserInfo(providerUserId, email, firstName, lastName, avatarUrl);
    }

    @SuppressWarnings("unchecked")
    private String fetchPrimaryEmail(String accessToken) {
        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri(EMAILS_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(List.class);
            if (emails == null) return null;
            for (Map<String, Object> entry : emails) {
                Object primary = entry.get("primary");
                Object verified = entry.get("verified");
                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    return asString(entry.get("email"));
                }
            }
        } catch (Exception ignored) {
            // best-effort; email remains null
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
