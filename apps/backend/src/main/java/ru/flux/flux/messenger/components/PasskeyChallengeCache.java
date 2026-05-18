package ru.flux.flux.messenger.components;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class PasskeyChallengeCache {

    public record PendingRegistration(
            PublicKeyCredentialCreationOptions options,
            String phone,
            String firstName,
            String lastName,
            String username
    ) {}

    private final Cache<String, PendingRegistration> registrationCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(10_000)
                    .build();

    private final Cache<String, PublicKeyCredentialRequestOptions> authenticationCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(10_000)
                    .build();

    // Keyed by the base64url challenge string itself (extracted from clientDataJSON on finish)
    private final Cache<String, byte[]> assertionChallengeCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(10_000)
                    .build();

    public String saveRegistrationOptions(PublicKeyCredentialCreationOptions options,
                                          String phone, String firstName,
                                          String lastName, String username) {
        String nonce = UUID.randomUUID().toString();
        registrationCache.put(nonce, new PendingRegistration(options, phone, firstName, lastName, username));
        return nonce;
    }

    public PendingRegistration consumeRegistrationOptions(String nonce) {
        PendingRegistration pending = registrationCache.getIfPresent(nonce);
        registrationCache.invalidate(nonce);
        return pending;  // null if expired or already used
    }

    public String saveAuthenticationOptions(PublicKeyCredentialRequestOptions options) {
        String nonce = UUID.randomUUID().toString();
        authenticationCache.put(nonce, options);
        return nonce;
    }

    public PublicKeyCredentialRequestOptions consumeAuthenticationOptions(String nonce) {
        PublicKeyCredentialRequestOptions options = authenticationCache.getIfPresent(nonce);
        authenticationCache.invalidate(nonce);
        return options;
    }
}
