package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.PasskeyCredential;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, String> {
}
