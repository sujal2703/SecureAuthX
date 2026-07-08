package com.secureauthx.server.oidc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DiscoveryResponse(
        String issuer,
        @JsonProperty("authorization_endpoint") String authorizationEndpoint,
        @JsonProperty("token_endpoint") String tokenEndpoint,
        @JsonProperty("userinfo_endpoint") String userinfoEndpoint,
        @JsonProperty("jwks_uri") String jwksUri,
        @JsonProperty("response_types_supported") List<String> responseTypesSupported,
        @JsonProperty("subject_types_supported") List<String> subjectTypesSupported,
        @JsonProperty("id_token_signing_alg_values_supported") List<String> idTokenSigningAlgValuesSupported,
        @JsonProperty("scopes_supported") List<String> scopesSupported,
        @JsonProperty("claims_supported") List<String> claimsSupported,
        @JsonProperty("claims_parameter_supported") boolean claimsParameterSupported,
        @JsonProperty("request_parameter_supported") boolean requestParameterSupported,
        @JsonProperty("request_uri_parameter_supported") boolean requestUriParameterSupported
) {}
