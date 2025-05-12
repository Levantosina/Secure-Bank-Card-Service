package io.github.levantosina.bankcardmanagement.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public  record UserUpdateRequest (
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Please provide a valid email address")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 9, message = "Password must be at least 9 characters long")
        String password
){
}
