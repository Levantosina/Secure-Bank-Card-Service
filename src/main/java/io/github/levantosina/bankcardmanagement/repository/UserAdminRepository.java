package io.github.levantosina.bankcardmanagement.repository;

import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAdminRepository extends JpaRepository<UserAdminEntity, Long> {
    boolean existsUserAdminByEmail(String username);
    Optional<UserAdminEntity>findByEmail(String email);
    boolean existsUserAdminByUserId(Long userId);
    Optional<UserAdminEntity>findUserAdminByUserId(Long userId);
    void deleteUserAdminByUserId(Long userId);

}
