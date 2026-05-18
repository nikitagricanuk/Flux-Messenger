package ru.flux.flux.messenger.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.flux.flux.messenger.Message;
import ru.flux.flux.messenger.MessageStatus;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query(
        value = "SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.chat WHERE m.chat.id = :chatId ORDER BY m.createdAt ASC",
        countQuery = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId"
    )
    Page<Message> findByChatIdOrderByCreatedAtAsc(@Param("chatId") UUID chatId, Pageable pageable);

    Message findTopByChatIdOrderByCreatedAtDesc(UUID chatId);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.status != :status")
    void updateStatusForChat(UUID chatId, UUID userId, MessageStatus status);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chat.id = :chatId AND m.mediaUrl IS NOT NULL")
    List<Message> findByChatIdAndMediaUrlNotNull(@Param("chatId") UUID chatId);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chat.id IN :chatIds AND m.createdAt = (SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.chat.id = m.chat.id)")
    List<Message> findLastMessagesForChats(@Param("chatIds") List<UUID> chatIds);
}