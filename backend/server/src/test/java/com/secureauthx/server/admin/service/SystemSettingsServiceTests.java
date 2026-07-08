package com.secureauthx.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.admin.dto.SystemSettingRequest;
import com.secureauthx.server.admin.dto.SystemSettingResponse;
import com.secureauthx.server.admin.entity.SystemSetting;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.SystemSettingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemSettingsServiceTests {

    @Mock private SystemSettingRepository settingRepository;

    private SystemSettingsService systemSettingsService;

    @BeforeEach
    void setUp() {
        systemSettingsService = new SystemSettingsService(settingRepository);
    }

    @Test
    void getAllSettingsReturnsAll() {
        SystemSetting setting = new SystemSetting("maintenance_mode", "false", "desc");
        setField(setting, "id", UUID.randomUUID());
        when(settingRepository.findAll()).thenReturn(List.of(setting));

        List<SystemSettingResponse> settings = systemSettingsService.getAllSettings();
        assertThat(settings).hasSize(1);
        assertThat(settings.getFirst().settingKey()).isEqualTo("maintenance_mode");
    }

    @Test
    void getSettingReturnsByKey() {
        SystemSetting setting = new SystemSetting("registration_enabled", "true", "desc");
        setField(setting, "id", UUID.randomUUID());
        when(settingRepository.findBySettingKey("registration_enabled")).thenReturn(Optional.of(setting));

        SystemSettingResponse response = systemSettingsService.getSetting("registration_enabled");
        assertThat(response.settingValue()).isEqualTo("true");
    }

    @Test
    void getSettingThrowsWhenNotFound() {
        when(settingRepository.findBySettingKey("unknown")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> systemSettingsService.getSetting("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSettingUpdatesValue() {
        SystemSetting setting = new SystemSetting("maintenance_mode", "false", "desc");
        setField(setting, "id", UUID.randomUUID());
        when(settingRepository.findBySettingKey("maintenance_mode")).thenReturn(Optional.of(setting));
        when(settingRepository.save(any(SystemSetting.class))).thenAnswer(inv -> inv.getArgument(0));

        SystemSettingRequest request = new SystemSettingRequest("true");
        SystemSettingResponse response = systemSettingsService.updateSetting("maintenance_mode", request);

        assertThat(response.settingValue()).isEqualTo("true");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
