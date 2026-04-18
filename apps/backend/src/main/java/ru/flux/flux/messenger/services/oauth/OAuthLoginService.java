package ru.flux.flux.messenger.services.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    public static final String CLAIM_PROVIDER = "provider";
    public static final String CLAIM_PROVIDER_USER_ID = "providerUserId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_FIRST_NAME = "firstName";
    public static final String CLAIM_LAST_NAME = "lastName";
    public static final String CLAIM_AVATAR_URL = "avatarUrl";

    private final UserService userService;
    private final JwtService jwtService;

    public OAuthLoginResponse handle(OAuthProvider provider, OAuthUserInfo info) {
        Optional<User> linked = userService.findLinkedUser(provider, info.providerUserId());
        if (linked.isPresent()) {
            return loggedIn(linked.get());
        }
        if (info.email() != null && !info.email().isBlank()) {
            Optional<User> byEmail = userService.findByEmail(info.email());
            if (byEmail.isPresent()) {
                User user = byEmail.get();
                userService.linkOAuthIdentity(user, provider, info.providerUserId(), info.email());
                return loggedIn(user);
            }
        }
        return needsProfile(provider, info);
    }

    private OAuthLoginResponse loggedIn(User user) {
        return OAuthLoginResponse.builder()
                .status(OAuthLoginResponse.STATUS_LOGGED_IN)
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    private OAuthLoginResponse needsProfile(OAuthProvider provider, OAuthUserInfo info) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_PROVIDER, provider.name());
        claims.put(CLAIM_PROVIDER_USER_ID, info.providerUserId());
        if (info.email() != null) claims.put(CLAIM_EMAIL, info.email());
        if (info.firstName() != null) claims.put(CLAIM_FIRST_NAME, info.firstName());
        if (info.lastName() != null) claims.put(CLAIM_LAST_NAME, info.lastName());
        if (info.avatarUrl() != null) claims.put(CLAIM_AVATAR_URL, info.avatarUrl());

        String subject = provider.name() + ":" + info.providerUserId();
        String token = jwtService.generateRegistrationToken(subject, claims);

        return OAuthLoginResponse.builder()
                .status(OAuthLoginResponse.STATUS_NEEDS_PROFILE)
                .registrationToken(token)
                .suggestedEmail(info.email())
                .suggestedFirstName(info.firstName())
                .suggestedLastName(info.lastName())
                .suggestedAvatarUrl(info.avatarUrl())
                .build();
    }
}
