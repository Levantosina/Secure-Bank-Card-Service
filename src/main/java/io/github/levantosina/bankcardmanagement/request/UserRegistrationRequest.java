package io.github.levantosina.bankcardmanagement.request;


import io.github.levantosina.bankcardmanagement.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @Email(message = "Please provide a valid email address")
        String email,
        @Size(min = 9)
        @NotEmpty
        String password,
        Role role
) {
    public UserRegistrationRequest {
        if (role == null) {
            role = Role.ROLE_USER;
        }
    }
}