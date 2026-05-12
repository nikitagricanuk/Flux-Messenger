package ru.flux.flux.messenger.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.UserOAuthIdentity;
import ru.flux.flux.messenger.dto.*;
import ru.flux.flux.messenger.exceptions.UserAlreadyExistsException;
import ru.flux.flux.messenger.exceptions.UserNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.UserOAuthIdentityRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final ChatRepository chatRepository;
    private final UserOAuthIdentityRepository oauthIdentityRepository;
    private final StorageService storageService;

    public UserService(UserRepository repository,
                       ChatRepository chatRepository,
                       UserOAuthIdentityRepository oauthIdentityRepository,
                       StorageService storageService) {
        this.repository = repository;
        this.chatRepository = chatRepository;
        this.oauthIdentityRepository = oauthIdentityRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toResponse(user);
    }

    @Transactional
    public User registerUser(SignUpRequest request, String encodedPassword) {
        if (repository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("User with phone " + request.getPhone() + " already exists");
        }
        if (repository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + request.getUsername() + " already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .password(encodedPassword)
                .notifications(true)
                .build();

        return repository.save(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }
        else if (repository.existsByPhone(request.phone())) {
            throw new UserAlreadyExistsException("User with phone " + request.phone() + " already exists");
        }
        else if (repository.existsByUsername(request.nickname())) {
            throw new UserAlreadyExistsException("User with username " + request.nickname() + " already exists");
        }
        User user = new User();
        applyRequest(user, request);

        User saved = repository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(UUID id, CreateUserRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        applyRequest(user, request);

        User saved = repository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public void deleteUserById(UUID id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(UUID userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getContactIds().stream().map(contactId -> {
            User contact = repository.findById(contactId)
                    .orElseThrow(() -> new UserNotFoundException(contactId));

            String name = contact.getLastName() != null
                    ? contact.getFirstName() + " " + contact.getLastName()
                    : contact.getFirstName();

            String contactInfo = contact.getPhone() != null ? contact.getPhone() : contact.getEmail();

            List<SharedGroupInfo> sharedGroups = chatRepository.findSharedGroups(userId, contactId)
                    .stream()
                    .map(chat -> new SharedGroupInfo(chat.getId(), chat.getAvatarUrl()))
                    .toList();

            return new ContactResponse(contact.getId(), name, contactInfo, contact.getAvatarUrl(), sharedGroups);
        }).toList();
    }

    @Transactional
    public void addContact(UUID userId, UUID contactId) {
        if (userId.equals(contactId)) {
            throw new IllegalArgumentException("Cannot add yourself as a contact");
        }

        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        User contact = repository.findById(contactId)
                .orElseThrow(() -> new UserNotFoundException(contactId));

        if (!repository.existsById(contactId)) {
            throw new UserNotFoundException(contactId);
        }

        if (user.getContactIds().contains(contactId)) {
            throw new IllegalStateException("Contact already exists");
        }

        user.addContact(contact);
        repository.save(user);
    }

    @Transactional
    public void addContact(UUID userId, AddContactRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        User contact = repository.findByPhone(request.phone())
                .orElseThrow(() -> new UserNotFoundException(request.phone()));

        if (userId.equals(contact.getId())) {
            throw new IllegalArgumentException("Cannot add yourself as a contact");
        }

        if (user.getContactIds().contains(contact.getId())) {
            throw new IllegalStateException("Contact already exists");
        }

        user.addContact(contact, request.firstName(), request.lastName());
        repository.save(user);
    }

    @Transactional
    public void removeContact(UUID userId, UUID contactId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        User contact = repository.findById(contactId)
                .orElseThrow(() -> new UserNotFoundException(contactId));

        user.removeContact(contact);

        repository.save(user);
    }

    @Transactional
    public UserResponse uploadAvatar(UUID userId, MultipartFile file) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        try {
            String ext = org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename());
            String objectName = userId + (ext != null ? "." + ext : "");
            String url = storageService.upload(objectName, file.getInputStream(), file.getSize(), file.getContentType());
            user.setAvatarUrl(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
        return toResponse(repository.save(user));
    }

    private void applyRequest(User user, CreateUserRequest request) {
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        // Required fields: only update if non-blank (prevent accidental clearing)
        if (request.nickname() != null && !request.nickname().isBlank()) {
            user.setUsername(request.nickname());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }
        // Optional fields: null means clear
        user.setDateOfBirth(request.dateOfBirth());
        user.setEmail(request.email() != null && request.email().isBlank() ? null : request.email());
        user.setBio(request.bio());
        user.setAvatarUrl(request.avatarUrl());
        user.setNotifications(Boolean.TRUE.equals(request.notifications()));
    }

    public UserDetailsService userDetailsService() {
        return phone -> repository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + phone));
    }

    @Transactional(readOnly = true)
    public Optional<User> findLinkedUser(OAuthProvider provider, String providerUserId) {
        return oauthIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .flatMap(identity -> repository.findById(identity.getUserId()));
    }

    @Transactional
    public User createOAuthUser(OAuthProvider provider,
                                String providerUserId,
                                String email,
                                String phone,
                                String username,
                                String firstName,
                                String lastName,
                                String avatarUrl,
                                String encodedPlaceholderPassword) {
        if (repository.existsByPhone(phone)) {
            throw new UserAlreadyExistsException("User with phone " + phone + " already exists");
        }
        if (repository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User with username " + username + " already exists");
        }
        if (email != null && !email.isBlank() && repository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        User user = User.builder()
                .firstName(firstName != null && !firstName.isBlank() ? firstName : username)
                .lastName(lastName)
                .username(username)
                .phone(phone)
                .email(email)
                .avatarUrl(avatarUrl)
                .password(encodedPlaceholderPassword)
                .notifications(true)
                .build();

        User saved = repository.save(user);
        linkOAuthIdentity(saved, provider, providerUserId, email);
        return saved;
    }

    @Transactional
    public UserOAuthIdentity linkOAuthIdentity(User user, OAuthProvider provider, String providerUserId, String email) {
        UserOAuthIdentity identity = UserOAuthIdentity.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .userId(user.getId())
                .build();
        return oauthIdentityRepository.save(identity);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return repository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return repository.findById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getHandle(),
                user.getDateOfBirth(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.isNotifications(),
                user.getBio()
        );
    }
}
