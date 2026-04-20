package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "GitHub OAuth sign-in request")
public class GitHubOAuthRequest {
    @Schema(description = "Authorization code returned by GitHub")
    @NotBlank
    private String code;

    @Schema(description = "PKCE code verifier used to obtain the code")
    private String codeVerifier;

    @Schema(description = "Redirect URI originally passed to GitHub")
    @NotBlank
    private String redirectUri;
}
