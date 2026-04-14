package ru.flux.flux.messenger.dto;

import java.util.List;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        String name,
        String contact,
        String avatarUrl,
        List<SharedGroupInfo> sharedGroups
) {
}
