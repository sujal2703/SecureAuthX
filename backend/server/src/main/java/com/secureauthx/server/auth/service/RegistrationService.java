package com.secureauthx.server.auth.service;

import com.secureauthx.server.auth.dto.RegistrationRequest;
import com.secureauthx.server.auth.dto.RegistrationResponse;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.exception.DuplicateEmailException;
import com.secureauthx.server.auth.mapper.UserMapper;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RoleRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = userRepository.save(new User(normalizedEmail, passwordHash));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not found"));
        userRoleRepository.save(new UserRole(user, userRole));

        LOGGER.info("User registration completed for user_id={} with role=USER", user.getId());
        return userMapper.toRegistrationResponse(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
