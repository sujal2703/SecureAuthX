package com.secureauthx.server.auth.mapper;

import com.secureauthx.server.auth.dto.RegistrationResponse;
import com.secureauthx.server.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegistrationResponse toRegistrationResponse(User user) {
        return new RegistrationResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
