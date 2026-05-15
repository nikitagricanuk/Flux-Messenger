package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank String firstName,
        String lastName,
        @Size(min = 3, max = 32) @Pattern(regexp = "^[a-zA-Z0-9_]+$") String nickname,
        LocalDate dateOfBirth,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
        @Email String email,
        String avatarUrl,
        Boolean notifications,
        String bio
) {
}
