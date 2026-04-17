package ru.flux.flux.messenger.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.flux.flux.messenger.ChatType;
import ru.flux.flux.messenger.dto.CreateChatRequest;

public class ChatRequestValidator implements ConstraintValidator<ValidChatRequest, CreateChatRequest> {

    @Override
    public boolean isValid(CreateChatRequest request, ConstraintValidatorContext ctx) {
        if (request.type() == null) return true; // let @NotNull handle it

        ctx.disableDefaultConstraintViolation();
        boolean valid = true;

        if (request.type() == ChatType.GROUP) {
            if (request.name() == null || request.name().isBlank()) {
                ctx.buildConstraintViolationWithTemplate("Group chats require a name")
                        .addPropertyNode("name")
                        .addConstraintViolation();
                valid = false;
            }
            if (request.avatarUrl() == null || request.avatarUrl().isBlank()) {
                ctx.buildConstraintViolationWithTemplate("Group chats require a profile picture")
                        .addPropertyNode("avatarUrl")
                        .addConstraintViolation();
                valid = false;
            }
        } else if (request.type() == ChatType.DIRECT) {
            if (request.name() != null) {
                ctx.buildConstraintViolationWithTemplate("Direct chats cannot have a name")
                        .addPropertyNode("name")
                        .addConstraintViolation();
                valid = false;
            }
            if (request.avatarUrl() != null) {
                ctx.buildConstraintViolationWithTemplate("Direct chats cannot have a profile picture")
                        .addPropertyNode("avatarUrl")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
