package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.Profile;

import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}
