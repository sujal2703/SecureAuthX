package com.secureauthx.server.common.exception;

import com.secureauthx.server.auth.exception.DuplicateEmailException;
import com.secureauthx.server.auth.exception.InvalidCredentialsException;
import com.secureauthx.server.auth.exception.InvalidTokenException;
import com.secureauthx.server.oauth.exception.InvalidClientException;
import com.secureauthx.server.oauth.exception.InvalidGrantException;
import com.secureauthx.server.oauth.exception.InvalidRedirectUriException;
import com.secureauthx.server.oauth.exception.InvalidScopeException;
import com.secureauthx.server.oauth.exception.OAuthException;
import com.secureauthx.server.oauth.exception.UnauthorizedClientException;
import com.secureauthx.server.organization.exception.OrganizationAccessDeniedException;
import com.secureauthx.server.organization.exception.OrganizationNotFoundException;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.exception.DeveloperApiKeyNotFoundException;
import com.secureauthx.server.developer.exception.DeveloperAccessDeniedException;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.passkey.exception.PasskeyNotFoundException;
import com.secureauthx.server.passkey.exception.WebAuthnException;
import com.secureauthx.server.sessions.exception.SessionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed.",
                request.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Email is already registered.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidToken(
            InvalidTokenException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(SessionNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleSessionNotFound(
            SessionNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleOrganizationNotFound(
            OrganizationNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OrganizationAccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleOrganizationAccessDenied(
            OrganizationAccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OAuthException.class)
    ResponseEntity<ApiErrorResponse> handleOAuthException(
            OAuthException exception,
            HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("error", exception.getErrorCode());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(WebAuthnException.class)
    ResponseEntity<ApiErrorResponse> handleWebAuthnException(
            WebAuthnException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(PasskeyNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handlePasskeyNotFound(
            PasskeyNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DeveloperProjectNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleDeveloperProjectNotFound(
            DeveloperProjectNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DeveloperApiKeyNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleDeveloperApiKeyNotFound(
            DeveloperApiKeyNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DeveloperAccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleDeveloperAccessDenied(
            DeveloperAccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn("Database constraint violation while processing request path={}", request.getRequestURI());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Request conflicts with an existing resource.",
                request.getRequestURI(),
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        ));
    }
}
