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

    private final Cache<String, PublicKeyCredentialCreationOptions> registrationCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(10_000)
                    .build();

    private final Cache<String, PublicKeyCredentialRequestOptions> authenticationCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .maximumSize(10_000)
                    .build();

    public String saveRegistrationOptions(PublicKeyCredentialCreationOptions options) {
        String nonce = UUID.randomUUID().toString();
        registrationCache.put(nonce, options);
        return nonce;
    }

    public PublicKeyCredentialCreationOptions consumeRegistrationOptions(String nonce) {
        PublicKeyCredentialCreationOptions options = registrationCache.getIfPresent(nonce);
        registrationCache.invalidate(nonce);
        return options;  // null if expired or already used
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
