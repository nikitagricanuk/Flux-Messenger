package ru.flux.flux.messenger.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChatRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChatRequest {
    String message() default "Invalid chat request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
