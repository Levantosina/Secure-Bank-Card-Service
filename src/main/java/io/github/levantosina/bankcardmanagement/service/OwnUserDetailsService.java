package io.github.levantosina.bankcardmanagement.service;

import io.github.levantosina.bankcardmanagement.model.OwnDetails;
import io.github.levantosina.bankcardmanagement.repository.UserAdminRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OwnUserDetailsService implements UserDetailsService {

    private final UserAdminRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        //System.out.println("Authorization for " + user.getEmail() + ", Role: " + user.getRole());
        return new OwnDetails(user);
    }
}