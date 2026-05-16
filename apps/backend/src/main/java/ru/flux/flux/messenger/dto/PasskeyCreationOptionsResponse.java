package ru.flux.flux.messenger.dto;

import java.util.List;

public record PasskeyCreationOptionsResponse(
        String challenge,
        Rp rp,
        User user,
        List<PubKeyCredParam> pubKeyCredParams,
        long timeout,
        String attestation,
        List<Object> excludeCredentials,
        AuthenticatorSelection authenticatorSelection
) {
    public record Rp(String id, String name) {}

    public record User(String id, String name, String displayName) {}

    public record PubKeyCredParam(String type, long alg) {}

    public record AuthenticatorSelection(String residentKey, String userVerification) {}
}