package com.edusphere.error;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsResponseStatusExceptionAndPreservesRequestId() {
        MockHttpServletRequest request = request("/api/v1/students", "req-123");

        var response = handler.handleResponseStatus(
                new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Student not found"),
                request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Student not found", response.getBody().message());
        assertEquals("req-123", response.getBody().requestId());
        assertEquals("req-123", response.getHeaders().getFirst("X-Request-ID"));
    }

    @Test
    void mapsAccessDeniedToForbiddenWithoutInternalMessage() {
        MockHttpServletRequest request = request("/api/v1/admin", null);

        var response = handler.handleAccessDenied(new AccessDeniedException("internal security detail"), request);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Access denied", response.getBody().message());
    }

    @Test
    void mapsIntegrityViolationToConflict() {
        MockHttpServletRequest request = request("/api/v1/fees", null);

        var response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("database constraint details"), request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Request conflicts with existing data", response.getBody().message());
    }

    @Test
    void unexpectedExceptionsDoNotLeakInternalDetails() {
        MockHttpServletRequest request = request("/api/v1/students", null);

        var response = handler.handleUnexpected(new IllegalStateException("secret database detail"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal server error", response.getBody().message());
    }

    private MockHttpServletRequest request(String uri, String requestId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        if (requestId != null) request.addHeader("X-Request-ID", requestId);
        return request;
    }
}
