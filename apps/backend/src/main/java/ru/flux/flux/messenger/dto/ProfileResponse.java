package ru.flux.flux.messenger.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String nickname,
        LocalDate dateOfBirth,
        String phone,
        String email,
        boolean notifications
) {
}
