package io.github.levantosina.bankcardmanagement.request;

import io.github.levantosina.bankcardmanagement.model.Role;

public record AdminRegistrationRequest(String email, String password, Role role) implements RegistrationRequest {
    public Role role() {
        return role != null ? role : Role.ROLE_ADMIN;
    }
}