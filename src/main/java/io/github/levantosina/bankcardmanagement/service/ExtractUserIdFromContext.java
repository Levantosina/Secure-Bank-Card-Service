package io.github.levantosina.bankcardmanagement.service;

import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Component
@AllArgsConstructor
public class ExtractUserIdFromContext {
    private final UserAdminRepository userAdminRepository;

    public Long extractUserIdFromContext() throws AccessDeniedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new AccessDeniedException("No authenticated user found");
        }
        String authenticatedEmail = userDetails.getUsername();

        UserAdminEntity authenticatedUser = userAdminRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));

        return authenticatedUser.getUserId();
    }
}