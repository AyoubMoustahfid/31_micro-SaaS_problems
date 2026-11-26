package com.example.microSaas.platform.exception;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(
            Map.of(
                "timestamp", Instant.now(),
                "status", exception.getStatus().value(),
                "error", exception.getStatus().name(),
                "message", exception.getMessage()
            )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(
        IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().body(
            Map.of(
                "timestamp", Instant.now(),
                "status", 400,
                "error", "BAD_REQUEST",
                "message", exception.getMessage()
            )
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingRequestParameter(
        MissingServletRequestParameterException exception
    ) {
        return ResponseEntity.badRequest().body(
            Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "BAD_REQUEST",
                "message", "Required request parameter '" + exception.getParameterName() + "' is missing"
            )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleInvalidRequestParameter(
        MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.badRequest().body(
            Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "BAD_REQUEST",
                "message", "Request parameter '" + exception.getName()
                    + "' has an invalid value"
            )
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleSizeExceeded() {
        return ResponseEntity.status(
            HttpStatus.PAYLOAD_TOO_LARGE
        ).body(
            Map.of(
                "timestamp", Instant.now(),
                "status", 413,
                "error", "PAYLOAD_TOO_LARGE",
                "message", "Upload size exceeded"
            )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleInternalError(
        Exception exception
    ) {
        logger.error("Unhandled API exception", exception);
        return ResponseEntity.internalServerError().body(
            Map.of(
                "timestamp", Instant.now(),
                "status", 500,
                "error", "INTERNAL_SERVER_ERROR",
                "message", "An unexpected error occurred"
            )
        );
    }
}