package io.github.levantosina.bankcardmanagement.request;

import jakarta.validation.constraints.*;

public record UserRegistrationRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Invalid email format")   @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Invalid email format"
        )String email,
        @NotNull(message = "Password cannot be null") @Size(min = 9, message = "Password must be at least 9 characters") String password)
        implements RegistrationRequest {}
