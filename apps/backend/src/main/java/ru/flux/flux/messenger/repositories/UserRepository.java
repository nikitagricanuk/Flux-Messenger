package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
