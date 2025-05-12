package io.github.levantosina.bankcardmanagement.request;

import io.github.levantosina.bankcardmanagement.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminRegistrationRequest(
        @NotNull(message = "Email cannot be null") @Email(message = "Invalid email format")  @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Invalid email format"
        ) String email,
        @NotNull(message = "Password cannot be null") @Size(min = 9, message = "Password must be at least 9 characters") String password,
        Role role)

        implements RegistrationRequest {
    public Role role() {
        return role != null ? role : Role.ROLE_ADMIN;
    }
}