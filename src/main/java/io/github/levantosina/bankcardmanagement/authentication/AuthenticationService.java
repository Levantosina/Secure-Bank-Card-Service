package io.github.levantosina.bankcardmanagement.authentication;

import io.github.levantosina.bankcardmanagement.exception.BadCredentialsException;
import io.github.levantosina.bankcardmanagement.jwt.JwtUtil;
import io.github.levantosina.bankcardmanagement.model.OwnDetails;
import io.github.levantosina.bankcardmanagement.model.Role;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import io.github.levantosina.bankcardmanagement.request.AdminRegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.RegistrationRequest;
import io.github.levantosina.bankcardmanagement.request.UserRegistrationRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserAdminRepository userAdminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void createUser(RegistrationRequest registrationRequest, boolean isAdminRequest) {
        if (userAdminRepository.existsUserAdminByEmail(registrationRequest.email())) {
            throw new IllegalArgumentException("User already exists");
        }

        UserAdminEntity userAdminEntity = new UserAdminEntity();
        userAdminEntity.setEmail(registrationRequest.email());
        userAdminEntity.setPassword(passwordEncoder.encode(registrationRequest.password()));

        if (isAdminRequest && registrationRequest instanceof AdminRegistrationRequest adminReq) {
            Role role = adminReq.role();
            if (!List.of(Role.ROLE_USER, Role.ROLE_ADMIN).contains(role)) {
                throw new IllegalArgumentException("Invalid role: " + role.getRole());
            }
            userAdminEntity.setRole(role);
        } else {
            userAdminEntity.setRole(Role.ROLE_USER);
        }

        userAdminRepository.save(userAdminEntity);
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        log.info("Attempting to authenticate user with username: {}", authenticationRequest.userName());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.userName(),
                            authenticationRequest.password()
                    )
            );

            OwnDetails ownUserDetails = (OwnDetails) authentication.getPrincipal();
            UserAdminEntity userAdminEntity = ownUserDetails.getUserEntity();

            log.info("Authenticated user: {}", userAdminEntity.getEmail());

            Role role = userAdminEntity.getRole();
            Long userId = userAdminEntity.getUserId();

            String token = jwtUtil.issueToken(userAdminEntity.getUsername(), userId, role);

            log.info("Generated token for user {} with role {}: {}", userAdminEntity.getUsername(), role, token);

            return new AuthenticationResponse(token);

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user {}: {}", authenticationRequest.userName(), ex.getMessage());
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
