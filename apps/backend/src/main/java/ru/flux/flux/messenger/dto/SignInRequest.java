package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Sign in request")
public class SignInRequest {
        @Schema(description = "Phone number", example = "+12345678901")
        @NotBlank(message = "Номер телефона не может быть пустым")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Номер телефона должен содержать от 10 до 15 цифр и может начинаться с +")
        String phone;

        @Schema(description = "Password")
        @NotBlank(message = "Пароль не может быть пустым")
        String password;
}