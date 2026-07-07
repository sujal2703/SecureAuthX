package com.secureauthx.server.authorization.service;

import com.secureauthx.server.authorization.dto.RoleResponse;
import com.secureauthx.server.authorization.repository.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .toList();
    }
}
