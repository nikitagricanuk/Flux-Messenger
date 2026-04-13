package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.flux.flux.messenger.Chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Override
    @Query(value = "SELECT * FROM chat", nativeQuery = true)
    List<Chat> findAll();

    @Override
    @Query(value = "SELECT * FROM chat WHERE id = :id", nativeQuery = true)
    Optional<Chat> findById(@Param("id") UUID id);

    @Query(value = """
            SELECT DISTINCT c.* FROM chat c
            WHERE c.type = :type
            AND (SELECT COUNT(*) FROM chat_member_ids m WHERE m.chat_id = c.id AND m.member_ids IN (:memberIds)) = :memberCount
            AND (SELECT COUNT(*) FROM chat_member_ids m2 WHERE m2.chat_id = c.id) = :memberCount
            """, nativeQuery = true)
    List<Chat> findByTypeAndExactMembers(@Param("type") String type, @Param("memberIds") List<UUID> memberIds, @Param("memberCount") long memberCount);
}
