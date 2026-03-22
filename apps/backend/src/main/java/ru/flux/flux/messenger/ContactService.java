package ru.flux.flux.messenger;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import ru.flux.flux.messenger.dto.ContactResponse;
import ru.flux.flux.messenger.dto.CreateContactRequest;
import ru.flux.flux.messenger.exceptions.ContactNotFoundException;

@Service
public class ContactService {
    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getAllContacts() {
        return repository.findAll()
        .stream()
        .map(this::toResponse)
        .toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse getContactById(UUID id) {
        return repository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new ContactNotFoundException(id));
    }

    @Transactional
    public  ContactResponse createContact(CreateContactRequest request)
    {
        Contact contact = new Contact(
                request.avatarUrl(),
                request.username(),
                request.name(),
                request.surname(),
                request.phoneNumber(),
                request.groups()
        );
        Contact saved = repository.save(contact);
        return toResponse(saved);
    }

    @Transactional
    public void deleteContactById(UUID id) {
        if(!repository.existsById(id)){
            throw new ContactNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getSurname(),
                contact.getPhoneNumber(),
                contact.getAvatarUrl(),
                contact.getUsername(),
                List.copyOf(contact.getGroups() != null ? contact.getGroups() : List.of())
        );
    }

}
