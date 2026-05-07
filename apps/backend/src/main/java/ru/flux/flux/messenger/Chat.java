package ru.flux.flux.messenger;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type;

    @Column
    private String name; // null for DIRECT, required for GROUP

    @Column
    private String avatarUrl; // null for DIRECT, required for GROUP

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMember> members = new ArrayList<>();

    public void addMember(User user) {
        ChatMember cm = new ChatMember();
        cm.setChat(this);
        cm.setUser(user);
        members.add(cm);
    }

    public void removeMember(User user) {
        members.removeIf(cm -> cm.getUser().equals(user));
    }

    public List<UUID> getMemberIds() {
        return members.stream()
                .map(cm -> cm.getUser().getId())
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chat chat)) return false;
        return id != null && id.equals(chat.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}