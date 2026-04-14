package ru.flux.flux.messenger.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.Profile;
import ru.flux.flux.messenger.dto.ContactResponse;
import ru.flux.flux.messenger.dto.CreateProfileRequest;
import ru.flux.flux.messenger.dto.ProfileResponse;
import ru.flux.flux.messenger.dto.SharedGroupInfo;
import ru.flux.flux.messenger.exceptions.ProfileNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.ProfileRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository repository;
    private final ChatRepository chatRepository;

    public ProfileService(ProfileRepository repository, ChatRepository chatRepository) {
        this.repository = repository;
        this.chatRepository = chatRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> getAllProfiles() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileById(UUID id) {
        Profile profile = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        Profile profile = new Profile();
        applyRequest(profile, request);

        Profile saved = repository.save(profile);
        return toResponse(saved);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID id, CreateProfileRequest request) {
        Profile profile = repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        applyRequest(profile, request);

        Profile saved = repository.save(profile);
        return toResponse(saved);
    }

    @Transactional
    public void deleteProfileById(UUID id) {
        if (!repository.existsById(id)) {
            throw new ProfileNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(UUID userId) {
        Profile user = repository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        return user.getContacts().stream().map(contactId -> {
            Profile contact = repository.findById(contactId)
                    .orElseThrow(() -> new ProfileNotFoundException(contactId));

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

        Profile user = repository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        if (!repository.existsById(contactId)) {
            throw new ProfileNotFoundException(contactId);
        }

        if (user.getContacts().contains(contactId)) {
            throw new IllegalStateException("Contact already exists");
        }

        user.getContacts().add(contactId);
        repository.save(user);
    }

    @Transactional
    public void removeContact(UUID userId, UUID contactId) {
        Profile user = repository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        if (!user.getContacts().remove(contactId)) {
            throw new ProfileNotFoundException(contactId);
        }

        repository.save(user);
    }

    private void applyRequest(Profile profile, CreateProfileRequest request) {
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setNickname(request.nickname());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setPhone(request.phone());
        profile.setEmail(request.email());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setNotifications(Boolean.TRUE.equals(request.notifications()));
    }

    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getNickname(),
                profile.getDateOfBirth(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getAvatarUrl(),
                profile.isNotifications()
        );
    }
}
