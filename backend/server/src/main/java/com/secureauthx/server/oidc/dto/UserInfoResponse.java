package com.secureauthx.server.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponse(
        String sub,
        String email,
        @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName
) {}
