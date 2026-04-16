package ru.flux.flux.messenger.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.ContactResponse;
import ru.flux.flux.messenger.dto.CreateUserRequest;
import ru.flux.flux.messenger.dto.UserResponse;
import ru.flux.flux.messenger.dto.SharedGroupInfo;
import ru.flux.flux.messenger.exceptions.UserNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final ChatRepository chatRepository;

    public UserService(UserRepository repository, ChatRepository chatRepository) {
        this.repository = repository;
        this.chatRepository = chatRepository;
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
    public UserResponse createUser(CreateUserRequest request) {
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

        return user.getContacts().stream().map(contactId -> {
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

        if (!repository.existsById(contactId)) {
            throw new UserNotFoundException(contactId);
        }

        if (user.getContacts().contains(contactId)) {
            throw new IllegalStateException("Contact already exists");
        }

        user.getContacts().add(contactId);
        repository.save(user);
    }

    @Transactional
    public void removeContact(UUID userId, UUID contactId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.getContacts().remove(contactId)) {
            throw new UserNotFoundException(contactId);
        }

        repository.save(user);
    }

    private void applyRequest(User user, CreateUserRequest request) {
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setNickname(request.nickname());
        user.setDateOfBirth(request.dateOfBirth());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setAvatarUrl(request.avatarUrl());
        user.setNotifications(Boolean.TRUE.equals(request.notifications()));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getNickname(),
                user.getDateOfBirth(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.isNotifications()
        );
    }
}
