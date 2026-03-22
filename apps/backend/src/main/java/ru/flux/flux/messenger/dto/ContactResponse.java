package ru.flux.flux.messenger.dto;

import java.util.List;
import java.util.UUID;

public record ContactResponse(
    UUID id,
    String name,
    String surname,
    String phoneNumber,
    String avatarUrl,
    String username,
    List<String> groups
) {}