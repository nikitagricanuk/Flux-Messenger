package ru.flux.flux.messenger;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type;

    @Column
    private String name; // null for DIRECT, required for GROUP

    @ElementCollection
    private List<UUID> memberIds = new ArrayList<>();

    protected Chat() {}

    public Chat(ChatType type, String name, List<UUID> memberIds) {
        this.type = type;
        this.name = name;
        this.memberIds = memberIds;
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