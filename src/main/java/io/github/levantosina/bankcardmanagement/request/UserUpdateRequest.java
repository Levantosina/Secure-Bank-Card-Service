package io.github.levantosina.bankcardmanagement.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public  record UserUpdateRequest (
        @Email(message = "Please provide a valid email address")
        String email,
        @Size(min = 9)
        String password
){
}
