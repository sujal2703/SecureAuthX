package com.secureauthx.server.authorization.service;

import com.secureauthx.server.authorization.dto.PermissionResponse;
import com.secureauthx.server.authorization.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::from)
                .toList();
    }
}
