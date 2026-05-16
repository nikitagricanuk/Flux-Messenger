package ru.flux.flux.messenger.dto;

import java.util.List;

public record PasskeyAssertionOptionsResponse(
        String challenge,
        String rpId,
        long timeout,
        String userVerification,
        List<Object> allowCredentials
) {}