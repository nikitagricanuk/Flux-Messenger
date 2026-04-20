package ru.flux.flux.messenger.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.flux.flux.messenger.dto.GitHubOAuthRequest;
import ru.flux.flux.messenger.dto.GoogleOAuthRequest;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.OAuthCompleteRequest;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;
import ru.flux.flux.messenger.services.oauth.GitHubOAuthService;
import ru.flux.flux.messenger.services.oauth.GoogleOAuthService;
import ru.flux.flux.messenger.services.oauth.OAuthCompletionService;

@Tag(name = "OAuth", description = "Sign in with external identity providers")
@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GitHubOAuthService gitHubOAuthService;
    private final OAuthCompletionService completionService;

    @PostMapping("/google")
    @Operation(summary = "Sign in with Google",
            description = "Verifies a Google ID token. If the user is known, returns JWT tokens. Otherwise, returns a registration token to be exchanged via /oauth/complete.")
    public ResponseEntity<OAuthLoginResponse> google(@RequestBody @Valid GoogleOAuthRequest request) {
        return ResponseEntity.ok(googleOAuthService.login(request));
    }

    @PostMapping("/github")
    @Operation(summary = "Sign in with GitHub",
            description = "Exchanges a GitHub authorization code for a user profile. Same response contract as /oauth/google.")
    public ResponseEntity<OAuthLoginResponse> github(@RequestBody @Valid GitHubOAuthRequest request) {
        return ResponseEntity.ok(gitHubOAuthService.login(request));
    }

    @PostMapping("/complete")
    @Operation(summary = "Complete OAuth registration",
            description = "Consumes a short-lived registration token and creates the user with the supplied phone/username.")
    public ResponseEntity<JwtAuthenticationResponse> complete(@RequestBody @Valid OAuthCompleteRequest request) {
        return ResponseEntity.ok(completionService.complete(request));
    }
}
