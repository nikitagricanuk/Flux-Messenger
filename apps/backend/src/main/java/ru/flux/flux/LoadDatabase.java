package ru.flux.flux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.ChatType;

import java.util.List;
import java.util.UUID;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(ChatRepository repository) {

        return args -> {
            UUID u1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID u2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
            UUID u3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

            log.info("Preloading {}", repository.save(new Chat(ChatType.DIRECT, null, List.of(u1, u2))));
            log.info("Preloading {}", repository.save(new Chat(ChatType.GROUP, "Test Group", List.of(u1, u2, u3))));
        };
    }
}
