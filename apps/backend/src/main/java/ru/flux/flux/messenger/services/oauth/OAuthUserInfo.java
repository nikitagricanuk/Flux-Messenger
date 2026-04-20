package ru.flux.flux.messenger.services.oauth;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        String firstName,
        String lastName,
        String avatarUrl
) {}
