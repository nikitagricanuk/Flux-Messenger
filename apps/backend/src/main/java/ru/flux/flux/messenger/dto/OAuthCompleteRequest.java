package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Complete OAuth registration by supplying phone and username")
public class OAuthCompleteRequest {
    @Schema(description = "Registration token returned by /oauth/{provider} when status=NEEDS_PROFILE")
    @NotBlank
    private String registrationToken;

    @Schema(description = "Phone number", example = "+12345678901")
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Номер телефона должен содержать от 10 до 15 цифр и может начинаться с +")
    private String phone;

    @Schema(description = "Username", example = "johndoe")
    @NotBlank
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;

    @Schema(description = "Optional override for first name")
    private String firstName;

    @Schema(description = "Optional override for last name")
    private String lastName;

    @Schema(description = "Optional override for avatar URL")
    private String avatarUrl;
}
