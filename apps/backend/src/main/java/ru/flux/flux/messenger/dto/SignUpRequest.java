package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Sign up request")
public class SignUpRequest {
        @Schema(description = "First name", example = "John")
        @NotBlank(message = "Имя не может быть пустым")
        String firstName;

        @Schema(description = "Last name", example = "Doe")
        String lastName;

        @Schema(description = "Username", example = "johndoe")
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 3, max = 32, message = "Имя пользователя должно содержать от 3 до 32 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Имя пользователя может содержать только латинские буквы, цифры и символ подчёркивания")
        String username;

        @Schema(description = "Phone number", example = "+12345678901")
        @NotBlank(message = "Номер телефона не может быть пустым")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Номер телефона должен содержать от 10 до 15 цифр и может начинаться с +")
        String phone;

        @Schema(description = "Avatar URL")
        String avatarUrl;

        @Schema(description = "Password")
        @NotBlank(message = "Пароль не может быть пустым")
        String password;
}
