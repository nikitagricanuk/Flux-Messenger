package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for OAuth provider login. Either LOGGED_IN with tokens, or NEEDS_PROFILE with a registration token that must be exchanged via /oauth/complete.")
public class OAuthLoginResponse {
    public static final String STATUS_LOGGED_IN = "LOGGED_IN";
    public static final String STATUS_NEEDS_PROFILE = "NEEDS_PROFILE";

    @Schema(description = "LOGGED_IN or NEEDS_PROFILE")
    private String status;

    @Schema(description = "Set when status=LOGGED_IN")
    private String accessToken;

    @Schema(description = "Set when status=LOGGED_IN")
    private String refreshToken;

    @Schema(description = "Set when status=NEEDS_PROFILE. Short-lived JWT used by /oauth/complete.")
    private String registrationToken;

    @Schema(description = "Suggested email from provider (for NEEDS_PROFILE)")
    private String suggestedEmail;

    @Schema(description = "Suggested first name from provider (for NEEDS_PROFILE)")
    private String suggestedFirstName;

    @Schema(description = "Suggested last name from provider (for NEEDS_PROFILE)")
    private String suggestedLastName;

    @Schema(description = "Suggested avatar URL from provider (for NEEDS_PROFILE)")
    private String suggestedAvatarUrl;
}
