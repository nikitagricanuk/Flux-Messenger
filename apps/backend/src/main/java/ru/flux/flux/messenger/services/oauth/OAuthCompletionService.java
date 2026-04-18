package ru.flux.flux.messenger.services.oauth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.OAuthCompleteRequest;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.UserService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthCompletionService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public JwtAuthenticationResponse complete(OAuthCompleteRequest request) {
        Claims claims = jwtService.parseRegistrationToken(request.getRegistrationToken());

        OAuthProvider provider;
        try {
            provider = OAuthProvider.valueOf(String.valueOf(claims.get(OAuthLoginService.CLAIM_PROVIDER)));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Registration token has invalid provider claim");
        }
        String providerUserId = String.valueOf(claims.get(OAuthLoginService.CLAIM_PROVIDER_USER_ID));
        if (providerUserId == null || providerUserId.isBlank() || "null".equals(providerUserId)) {
            throw new IllegalArgumentException("Registration token missing providerUserId claim");
        }

        Optional<User> existing = userService.findLinkedUser(provider, providerUserId);
        if (existing.isPresent()) {
            return buildResponse(existing.get());
        }

        String email = optionalClaim(claims, OAuthLoginService.CLAIM_EMAIL);
        String firstName = firstNonBlank(request.getFirstName(), optionalClaim(claims, OAuthLoginService.CLAIM_FIRST_NAME));
        String lastName = firstNonBlank(request.getLastName(), optionalClaim(claims, OAuthLoginService.CLAIM_LAST_NAME));
        String avatarUrl = firstNonBlank(request.getAvatarUrl(), optionalClaim(claims, OAuthLoginService.CLAIM_AVATAR_URL));

        String placeholderPassword = passwordEncoder.encode("oauth:" + UUID.randomUUID());

        User user = userService.createOAuthUser(
                provider,
                providerUserId,
                email,
                request.getPhone(),
                request.getUsername(),
                firstName,
                lastName,
                avatarUrl,
                placeholderPassword
        );

        return buildResponse(user);
    }

    private JwtAuthenticationResponse buildResponse(User user) {
        return JwtAuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    private static String optionalClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : value.toString();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
