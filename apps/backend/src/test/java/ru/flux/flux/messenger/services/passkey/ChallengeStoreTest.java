package ru.flux.flux.messenger.services.passkey;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChallengeStoreTest {

    private final ChallengeStore store = new ChallengeStore(
            Caffeine.newBuilder().maximumSize(128).build(),
            new SecureRandom()
    );

    @Test
    void generateChallengeReturns32Bytes() {
        byte[] challenge = store.generateChallenge();
        assertThat(challenge).hasSize(32);
    }

    @Test
    void storedChallengeCanBeConsumedExactlyOnce() {
        byte[] challenge = store.generateChallenge();
        UUID userId = UUID.randomUUID();
        store.store(challenge, new ChallengeContext(challenge, userId, ChallengeContext.Kind.REGISTRATION));

        ChallengeContext first = store.consume(challenge);
        assertThat(first).isNotNull();
        assertThat(first.userId()).isEqualTo(userId);
        assertThat(first.kind()).isEqualTo(ChallengeContext.Kind.REGISTRATION);

        ChallengeContext second = store.consume(challenge);
        assertThat(second).isNull();
    }

    @Test
    void unknownChallengeReturnsNull() {
        byte[] challenge = new byte[]{1, 2, 3};
        assertThat(store.consume(challenge)).isNull();
    }

    @Test
    void encodeProducesBase64UrlWithoutPadding() {
        byte[] bytes = new byte[]{(byte) 0xff, 0x00, 0x01, 0x7e};
        String encoded = store.encode(bytes);
        assertThat(encoded).doesNotContain("=");
        assertThat(encoded).doesNotContain("+").doesNotContain("/");
    }
}
