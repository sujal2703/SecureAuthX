package com.secureauthx.server.admin.repository;

import com.secureauthx.server.admin.entity.SystemSetting;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, UUID> {
    Optional<SystemSetting> findBySettingKey(String settingKey);
    boolean existsBySettingKey(String settingKey);
}
