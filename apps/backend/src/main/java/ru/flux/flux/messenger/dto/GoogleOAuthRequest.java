package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Google OAuth sign-in request")
public class GoogleOAuthRequest {
    @Schema(description = "Google-issued ID token")
    @NotBlank
    private String idToken;
}
