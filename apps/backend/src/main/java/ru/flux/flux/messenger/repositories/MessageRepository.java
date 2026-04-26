package ru.flux.flux.messenger.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.flux.flux.messenger.Message;
import ru.flux.flux.messenger.MessageStatus;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByChatIdOrderByCreatedAtAsc(UUID chatId, Pageable pageable);

    Message findTopByChatIdOrderByCreatedAtDesc(UUID chatId);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.status != :status")
    void updateStatusForChat(UUID chatId, UUID userId, MessageStatus status);
}