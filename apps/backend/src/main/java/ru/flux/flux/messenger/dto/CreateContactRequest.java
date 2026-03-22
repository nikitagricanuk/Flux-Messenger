package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateContactRequest(
    @NotBlank String name,
    @NotBlank String surname,
    @NotBlank String phoneNumber,
    String avatarUrl,
    @NotBlank String username,
    List<@NotBlank String> groups
) {} 
