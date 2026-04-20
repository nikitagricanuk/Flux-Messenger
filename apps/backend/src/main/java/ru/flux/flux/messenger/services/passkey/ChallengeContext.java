package ru.flux.flux.messenger.services.passkey;

import java.util.UUID;

public record ChallengeContext(byte[] challenge, UUID userId, Kind kind) {
    public enum Kind {
        REGISTRATION,
        AUTHENTICATION
    }
}
