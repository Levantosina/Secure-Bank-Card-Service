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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserAdminRepository userAdminRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterRegularUser() {
        RegistrationRequest request = new UserRegistrationRequest("user@example.com", "password");

        when(userAdminRepository.existsUserAdminByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        authenticationService.createUser(request, false);

        ArgumentCaptor<UserAdminEntity> captor = ArgumentCaptor.forClass(UserAdminEntity.class);
        verify(userAdminRepository).save(captor.capture());

        UserAdminEntity saved = captor.getValue();
        assertEquals("user@example.com", saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals(Role.ROLE_USER, saved.getRole());
    }

    @Test
    void shouldRegisterAdminUserWithValidRole() {
        RegistrationRequest request = new AdminRegistrationRequest("admin@example.com", "adminpass", Role.ROLE_ADMIN);

        when(userAdminRepository.existsUserAdminByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("adminpass")).thenReturn("encoded");

        authenticationService.createUser(request, true);

        ArgumentCaptor<UserAdminEntity> captor = ArgumentCaptor.forClass(UserAdminEntity.class);
        verify(userAdminRepository).save(captor.capture());

        assertEquals(Role.ROLE_ADMIN, captor.getValue().getRole());
    }

    @Test
    void shouldLoginSuccessfully() {
        AuthenticationRequest authRequest = new AuthenticationRequest("user@example.com", "password");

        UserAdminEntity user = new UserAdminEntity();
        user.setUserId(1L);
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);

        OwnDetails ownDetails = new OwnDetails(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(ownDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtUtil.issueToken("user@example.com", 1L, Role.ROLE_USER))
                .thenReturn("mocked-token");

        AuthenticationResponse response = authenticationService.login(authRequest);

        assertEquals("mocked-token", response.token());
    }

    @Test
    void shouldThrowOnLoginFailure() {
        AuthenticationRequest request = new AuthenticationRequest("wrong@example.com", "wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(request));
    }
}