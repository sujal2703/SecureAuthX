package com.secureauthx.server.oauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.oauth.dto.ClientResponse;
import com.secureauthx.server.oauth.dto.CreateClientRequest;
import com.secureauthx.server.oauth.dto.CreateClientResponse;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.entity.OAuthClientRedirectUri;
import com.secureauthx.server.oauth.exception.InvalidClientException;
import com.secureauthx.server.oauth.repository.OAuthClientRedirectUriRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OAuthClientServiceTests {

    @Mock
    private OAuthClientRepository oauthClientRepository;

    @Mock
    private OAuthClientRedirectUriRepository redirectUriRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<OAuthClient> clientCaptor;

    private OAuthClientService oauthClientService;

    @BeforeEach
    void setUp() {
        oauthClientService = new OAuthClientService(
                oauthClientRepository, redirectUriRepository, passwordEncoder
        );
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "hashed-" + inv.getArgument(0));
    }

    @Test
    void createsPublicClientSuccessfully() {
        when(oauthClientRepository.existsByClientId("my-client")).thenReturn(false);
        when(oauthClientRepository.save(any(OAuthClient.class))).thenAnswer(inv -> {
            OAuthClient client = inv.getArgument(0);
            java.lang.reflect.Field idField = OAuthClient.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(client, UUID.randomUUID());
            return client;
        });

        CreateClientRequest request = new CreateClientRequest(
                "my-client", null, "My Client", false, List.of("https://example.com/callback")
        );

        CreateClientResponse response = oauthClientService.createClient(request);

        assertThat(response.clientId()).isEqualTo("my-client");
        assertThat(response.clientSecret()).isNull();
        assertThat(response.confidential()).isFalse();

        verify(oauthClientRepository).save(clientCaptor.capture());
        OAuthClient saved = clientCaptor.getValue();
        assertThat(saved.getClientId()).isEqualTo("my-client");
        assertThat(saved.isConfidential()).isFalse();
    }

    @Test
    void createsConfidentialClientWithHashedSecret() {
        when(oauthClientRepository.existsByClientId("confidential-client")).thenReturn(false);
        when(oauthClientRepository.save(any(OAuthClient.class))).thenAnswer(inv -> {
            OAuthClient client = inv.getArgument(0);
            java.lang.reflect.Field idField = OAuthClient.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(client, UUID.randomUUID());
            return client;
        });

        CreateClientRequest request = new CreateClientRequest(
                "confidential-client", "my-secret", "Confidential Client",
                true, List.of("https://example.com/callback")
        );

        CreateClientResponse response = oauthClientService.createClient(request);

        assertThat(response.clientId()).isEqualTo("confidential-client");
        assertThat(response.clientSecret()).isEqualTo("my-secret");

        verify(oauthClientRepository).save(clientCaptor.capture());
        OAuthClient saved = clientCaptor.getValue();
        assertThat(saved.getHashedClientSecret()).isEqualTo("hashed-my-secret");
        assertThat(saved.isConfidential()).isTrue();
    }

    @Test
    void rejectsDuplicateClientId() {
        when(oauthClientRepository.existsByClientId("existing-client")).thenReturn(true);

        CreateClientRequest request = new CreateClientRequest(
                "existing-client", null, "Existing", false, List.of("https://example.com/callback")
        );

        assertThatThrownBy(() -> oauthClientService.createClient(request))
                .isInstanceOf(InvalidClientException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void rejectsConfidentialClientWithoutSecret() {
        CreateClientRequest request = new CreateClientRequest(
                "client", null, "Client", true, List.of("https://example.com/callback")
        );

        assertThatThrownBy(() -> oauthClientService.createClient(request))
                .isInstanceOf(InvalidClientException.class)
                .hasMessageContaining("must have a client secret");
    }

    @Test
    void getAllClientsReturnsList() {
        OAuthClient client1 = new OAuthClient("client-1", null, "Client 1", false);
        setField(client1, "id", UUID.randomUUID());
        OAuthClient client2 = new OAuthClient("client-2", "hashed-secret", "Client 2", true);
        setField(client2, "id", UUID.randomUUID());

        when(oauthClientRepository.findAll()).thenReturn(List.of(client1, client2));
        when(redirectUriRepository.findByClientId(client1.getId())).thenReturn(List.of(
                new OAuthClientRedirectUri(client1, "https://example1.com/callback")
        ));
        when(redirectUriRepository.findByClientId(client2.getId())).thenReturn(List.of(
                new OAuthClientRedirectUri(client2, "https://example2.com/callback")
        ));

        List<ClientResponse> clients = oauthClientService.getAllClients();

        assertThat(clients).hasSize(2);
        assertThat(clients.get(0).clientId()).isEqualTo("client-1");
        assertThat(clients.get(0).redirectUris()).containsExactly("https://example1.com/callback");
        assertThat(clients.get(1).clientId()).isEqualTo("client-2");
    }

    @Test
    void getClientReturnsClientWithRedirectUris() {
        UUID clientId = UUID.randomUUID();
        OAuthClient client = new OAuthClient("my-client", null, "My Client", false);
        setField(client, "id", clientId);

        when(oauthClientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(redirectUriRepository.findByClientId(clientId)).thenReturn(List.of(
                new OAuthClientRedirectUri(client, "https://example.com/callback")
        ));

        ClientResponse response = oauthClientService.getClient(clientId);

        assertThat(response.clientId()).isEqualTo("my-client");
        assertThat(response.redirectUris()).containsExactly("https://example.com/callback");
    }

    @Test
    void getClientThrowsForUnknownId() {
        UUID clientId = UUID.randomUUID();
        when(oauthClientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> oauthClientService.getClient(clientId))
                .isInstanceOf(InvalidClientException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void authenticateClientValidatesSecret() {
        OAuthClient client = new OAuthClient("confidential-client", "hashed-secret", "Confidential", true);
        setField(client, "id", UUID.randomUUID());

        when(oauthClientRepository.findByClientId("confidential-client")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("my-secret", "hashed-secret")).thenReturn(true);

        OAuthClient authenticated = oauthClientService.authenticateClient("confidential-client", "my-secret");
        assertThat(authenticated.getClientId()).isEqualTo("confidential-client");
    }

    @Test
    void authenticateClientRejectsInvalidSecret() {
        OAuthClient client = new OAuthClient("confidential-client", "hashed-secret", "Confidential", true);
        setField(client, "id", UUID.randomUUID());

        when(oauthClientRepository.findByClientId("confidential-client")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrong-secret", "hashed-secret")).thenReturn(false);

        assertThatThrownBy(() -> oauthClientService.authenticateClient("confidential-client", "wrong-secret"))
                .isInstanceOf(InvalidClientException.class)
                .hasMessageContaining("Invalid client secret");
    }

    @Test
    void authenticateClientRejectsDisabledClient() {
        OAuthClient client = new OAuthClient("disabled-client", null, "Disabled", false);
        setField(client, "id", UUID.randomUUID());
        java.lang.reflect.Field enabledField;
        try {
            enabledField = OAuthClient.class.getDeclaredField("enabled");
            enabledField.setAccessible(true);
            enabledField.set(client, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(oauthClientRepository.findByClientId("disabled-client")).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> oauthClientService.authenticateClient("disabled-client", null))
                .isInstanceOf(InvalidClientException.class)
                .hasMessageContaining("disabled");
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
