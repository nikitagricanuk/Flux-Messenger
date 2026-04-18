package ru.flux.flux.messenger.services.passkey;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Component
public class ChallengeStore {

    private static final int CHALLENGE_LEN = 32;

    private final Cache<String, ChallengeContext> cache;
    private final SecureRandom random;

    public ChallengeStore() {
        this(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build(), new SecureRandom());
    }

    ChallengeStore(Cache<String, ChallengeContext> cache, SecureRandom random) {
        this.cache = cache;
        this.random = random;
    }

    public byte[] generateChallenge() {
        byte[] bytes = new byte[CHALLENGE_LEN];
        random.nextBytes(bytes);
        return bytes;
    }

    public String encode(byte[] challenge) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challenge);
    }

    public void store(byte[] challenge, ChallengeContext context) {
        cache.put(encode(challenge), context);
    }

    public ChallengeContext consume(byte[] challenge) {
        String key = encode(challenge);
        ChallengeContext ctx = cache.getIfPresent(key);
        if (ctx != null) {
            cache.invalidate(key);
        }
        return ctx;
    }

    public ChallengeContext peek(byte[] challenge) {
        return cache.getIfPresent(encode(challenge));
    }
}
