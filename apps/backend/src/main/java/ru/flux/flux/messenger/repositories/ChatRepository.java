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

    @Query("""
    SELECT c FROM Chat c
    WHERE c.type = 'DIRECT'
    AND (SELECT COUNT(cm) FROM ChatMember cm WHERE cm.chat = c) = :memberCount
    AND (SELECT COUNT(cm) FROM ChatMember cm WHERE cm.chat = c AND cm.user.id IN :memberIds) = :memberCount
    """)
    List<Chat> findDirectChatWithExactMembers(
            @Param("memberIds") List<UUID> memberIds,
            @Param("memberCount") long memberCount
    );

    @Query("SELECT c FROM Chat c JOIN c.members cm WHERE cm.user.id = :userId")
    List<Chat> findByMemberId(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT c FROM Chat c JOIN FETCH c.members cm JOIN FETCH cm.user WHERE cm.user.id = :userId")
    List<Chat> findByMemberIdWithMembers(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT c FROM Chat c JOIN FETCH c.members cm JOIN FETCH cm.user WHERE c.id = :id")
    Optional<Chat> findWithMembersById(@Param("id") UUID id);

    @Query(value = """
        SELECT c.* FROM chat c
        WHERE c.type = 'GROUP'
        AND EXISTS (SELECT 1 FROM chat_member cm1 WHERE cm1.chat_id = c.id AND cm1.user_id = :userId)
        AND EXISTS (SELECT 1 FROM chat_member cm2 WHERE cm2.chat_id = c.id AND cm2.user_id = :contactId)
        """, nativeQuery = true)
    List<Chat> findSharedGroups(@Param("userId") UUID userId, @Param("contactId") UUID contactId);
}
