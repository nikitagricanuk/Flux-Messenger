package ru.flux.flux.messenger.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.flux.flux.messenger.dto.ContactResponse;
import ru.flux.flux.messenger.dto.CreateUserRequest;
import ru.flux.flux.messenger.dto.UserResponse;
import ru.flux.flux.messenger.services.UserService;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return service.getUserById(id);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return service.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable UUID id, @RequestBody CreateUserRequest request) {
        return service.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable UUID id) {
        service.deleteUserById(id);
    }

    @GetMapping("/{id}/contacts")
    public List<ContactResponse> getContacts(@PathVariable UUID id) {
        return service.getContacts(id);
    }

    @PutMapping("/{id}/contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addContact(@PathVariable UUID id, @PathVariable UUID contactId) {
        service.addContact(id, contactId);
    }

    @DeleteMapping("/{id}/contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeContact(@PathVariable UUID id, @PathVariable UUID contactId) {
        service.removeContact(id, contactId);
    }
}
