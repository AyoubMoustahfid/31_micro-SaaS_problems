package com.example.microSaas.platform.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Test
    void returnsApiExceptionStatusAndMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<?> response = handler.handleApiException(
                new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Antivirus scanning is unavailable"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Antivirus scanning is unavailable", body.get("message"));
        assertEquals("SERVICE_UNAVAILABLE", body.get("error"));
    }
}
