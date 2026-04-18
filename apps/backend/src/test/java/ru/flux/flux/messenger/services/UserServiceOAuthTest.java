package ru.flux.flux.messenger.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.UserOAuthIdentity;
import ru.flux.flux.messenger.exceptions.UserAlreadyExistsException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.UserOAuthIdentityRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceOAuthTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserOAuthIdentityRepository oauthIdentityRepository;

    @InjectMocks
    private UserService userService;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .username("alice")
                .phone("+12345678901")
                .email("alice@example.com")
                .password("x")
                .notifications(true)
                .build();
    }

    @Test
    void findLinkedUserReturnsLinkedUser() {
        UserOAuthIdentity identity = UserOAuthIdentity.builder()
                .provider(OAuthProvider.GOOGLE)
                .providerUserId("g-1")
                .userId(alice.getId())
                .build();
        when(oauthIdentityRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "g-1"))
                .thenReturn(Optional.of(identity));
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));

        Optional<User> result = userService.findLinkedUser(OAuthProvider.GOOGLE, "g-1");

        assertThat(result).contains(alice);
    }

    @Test
    void findLinkedUserReturnsEmptyWhenNoIdentity() {
        when(oauthIdentityRepository.findByProviderAndProviderUserId(any(), any()))
                .thenReturn(Optional.empty());
        assertThat(userService.findLinkedUser(OAuthProvider.GITHUB, "missing")).isEmpty();
    }

    @Test
    void linkOAuthIdentitySavesRecord() {
        ArgumentCaptor<UserOAuthIdentity> captor = ArgumentCaptor.forClass(UserOAuthIdentity.class);
        when(oauthIdentityRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.linkOAuthIdentity(alice, OAuthProvider.GITHUB, "gh-42", "alice@example.com");

        UserOAuthIdentity saved = captor.getValue();
        assertThat(saved.getProvider()).isEqualTo(OAuthProvider.GITHUB);
        assertThat(saved.getProviderUserId()).isEqualTo("gh-42");
        assertThat(saved.getUserId()).isEqualTo(alice.getId());
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void createOAuthUserRejectsDuplicatePhone() {
        when(userRepository.existsByPhone("+19999999999")).thenReturn(true);

        assertThatThrownBy(() -> userService.createOAuthUser(OAuthProvider.GOOGLE, "g-1",
                "a@b.com", "+19999999999", "newbie", "New", null, null, "pw"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("phone");
    }

    @Test
    void createOAuthUserRejectsDuplicateUsername() {
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.createOAuthUser(OAuthProvider.GOOGLE, "g-1",
                "a@b.com", "+12345678901", "taken", "N", null, null, "pw"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");
    }

    @Test
    void createOAuthUserPersistsUserAndIdentity() {
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(oauthIdentityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createOAuthUser(OAuthProvider.GITHUB, "gh-1",
                "alice@example.com", "+12345678901", "alice", "Alice", "Liddell", "https://a", "pw-hash");

        assertThat(created.getFirstName()).isEqualTo("Alice");
        assertThat(created.getLastName()).isEqualTo("Liddell");
        assertThat(created.getPhone()).isEqualTo("+12345678901");
        assertThat(created.getPassword()).isEqualTo("pw-hash");
        verify(oauthIdentityRepository).save(any(UserOAuthIdentity.class));
    }

    @Test
    void findByEmailHandlesBlankInput() {
        assertThat(userService.findByEmail(null)).isEmpty();
        assertThat(userService.findByEmail("")).isEmpty();
        assertThat(userService.findByEmail("   ")).isEmpty();
    }
}
