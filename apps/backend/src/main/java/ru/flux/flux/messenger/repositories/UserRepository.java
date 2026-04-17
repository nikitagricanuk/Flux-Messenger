package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}
