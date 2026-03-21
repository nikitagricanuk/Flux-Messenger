package ru.flux.flux.messenger.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.Profile;
import ru.flux.flux.messenger.dto.CreateProfileRequest;
import ru.flux.flux.messenger.dto.ProfileResponse;
import ru.flux.flux.messenger.exceptions.ProfileNotFoundException;
import ru.flux.flux.messenger.repositories.ProfileRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
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
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setNickname(request.nickname());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setPhone(request.phone());
        profile.setEmail(request.email());
        profile.setNotifications(Boolean.TRUE.equals(request.notifications()));

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

    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getNickname(),
                profile.getDateOfBirth(),
                profile.getPhone(),
                profile.getEmail(),
                profile.isNotifications()
        );
    }
}
