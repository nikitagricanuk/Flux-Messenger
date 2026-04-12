package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.ChatType;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query("""
            SELECT c FROM Chat c
            WHERE c.type = :type
            AND (SELECT COUNT(m) FROM Chat c2 JOIN c2.memberIds m WHERE c2 = c AND m IN :memberIds)
                = :memberCount
            AND SIZE(c.memberIds) = :memberCount
            """)
    List<Chat> findByTypeAndExactMembers(ChatType type, List<UUID> memberIds, long memberCount);
}
