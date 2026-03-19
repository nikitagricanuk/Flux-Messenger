package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.Chat;

import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
}
