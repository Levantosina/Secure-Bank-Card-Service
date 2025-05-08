package io.github.levantosina.bankcardmanagement.model;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;


@Getter
public class OwnDetails extends User {
    private final UserAdminEntity userEntity;

    public OwnDetails(UserAdminEntity userEntity) {
        super(userEntity.getUsername(), userEntity.getPassword(),
                getAuthorities(userEntity));
        this.userEntity = userEntity;
    }

    private static Collection<SimpleGrantedAuthority> getAuthorities(UserAdminEntity userEntity) {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (userEntity.getRole() == Role.ROLE_ADMIN) {
            authorities.add(new SimpleGrantedAuthority(Role.ROLE_ADMIN.getRole()));
        } else {
            authorities.add(new SimpleGrantedAuthority(Role.ROLE_USER.getRole()));
        }
        return authorities;
    }
}