package io.github.levantosina.bankcardmanagement.mapper;

import io.github.levantosina.bankcardmanagement.dto.UserAdminDTO;
import io.github.levantosina.bankcardmanagement.model.UserAdminEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserAdminDTOMapper implements Function<UserAdminEntity, UserAdminDTO> {

    @Override
    public UserAdminDTO apply(UserAdminEntity userEntity) {
        return new UserAdminDTO(
                userEntity.getEmail(),
                userEntity.getPassword()
        );
    }
}
