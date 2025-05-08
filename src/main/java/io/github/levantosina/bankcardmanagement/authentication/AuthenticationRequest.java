package io.github.levantosina.bankcardmanagement.authentication;

public record AuthenticationRequest(
        String userName,
        String password)
{ }
