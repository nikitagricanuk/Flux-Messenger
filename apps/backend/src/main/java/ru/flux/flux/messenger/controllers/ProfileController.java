package ru.flux.flux.messenger.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.flux.flux.messenger.dto.CreateProfileRequest;
import ru.flux.flux.messenger.dto.ProfileResponse;
import ru.flux.flux.messenger.services.ProfileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfileResponse> getAllProfiles() {
        return service.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ProfileResponse getProfileById(@PathVariable UUID id) {
        return service.getProfileById(id);
    }

    @PostMapping
    public ProfileResponse createProfile(@RequestBody CreateProfileRequest request) {
        return service.createProfile(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileById(@PathVariable UUID id) {
        service.deleteProfileById(id);
    }
}
