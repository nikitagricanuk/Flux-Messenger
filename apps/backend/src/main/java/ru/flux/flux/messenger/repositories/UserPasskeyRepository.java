package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.UserPasskey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPasskeyRepository extends JpaRepository<UserPasskey, UUID> {
    Optional<UserPasskey> findByCredentialId(byte[] credentialId);

    List<UserPasskey> findAllByUserId(UUID userId);
}
