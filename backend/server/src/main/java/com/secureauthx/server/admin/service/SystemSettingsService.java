package com.secureauthx.server.admin.service;

import com.secureauthx.server.admin.dto.SystemSettingRequest;
import com.secureauthx.server.admin.dto.SystemSettingResponse;
import com.secureauthx.server.admin.entity.SystemSetting;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.SystemSettingRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemSettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemSettingsService.class);

    private final SystemSettingRepository settingRepository;

    public SystemSettingsService(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAllSettings() {
        return settingRepository.findAll().stream()
                .map(SystemSettingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemSettingResponse getSetting(String key) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
        return SystemSettingResponse.from(setting);
    }

    @Transactional
    public SystemSettingResponse updateSetting(String key, SystemSettingRequest request) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
        setting.setSettingValue(request.settingValue());
        SystemSetting saved = settingRepository.save(setting);
        LOGGER.info("System setting updated key={} value={}", key, saved.getSettingValue());
        return SystemSettingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public String getSettingValue(String key) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(null);
    }
}
