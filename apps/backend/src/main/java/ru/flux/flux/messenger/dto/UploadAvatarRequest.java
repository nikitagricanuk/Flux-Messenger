package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadAvatarRequest(
        @NotBlank String avatarUrl
) {
}
