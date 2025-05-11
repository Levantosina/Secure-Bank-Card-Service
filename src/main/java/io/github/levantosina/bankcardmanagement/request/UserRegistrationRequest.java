package io.github.levantosina.bankcardmanagement.request;

public record UserRegistrationRequest(String email, String password) implements RegistrationRequest {}
