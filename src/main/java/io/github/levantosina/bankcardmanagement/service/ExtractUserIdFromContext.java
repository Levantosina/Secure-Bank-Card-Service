package io.github.levantosina.bankcardmanagement.service;

import io.github.levantosina.bankcardmanagement.model.OwnDetails;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Component
@AllArgsConstructor
public class ExtractUserIdFromContext {

    public Long extractUserIdFromContext() throws AccessDeniedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OwnDetails ownDetails)) {
            throw new AccessDeniedException("No authenticated user found");
        }

        UserAdminEntity authenticatedUser = ownDetails.getUserEntity();

        return authenticatedUser.getUserId();
    }
}
