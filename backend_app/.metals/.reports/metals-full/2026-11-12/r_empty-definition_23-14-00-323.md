error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/platform/exception/GloabalExceptionHandler.java:_empty_/ExceptionHandler#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/platform/exception/GloabalExceptionHandler.java
empty definition using pc, found symbol in pc: _empty_/ExceptionHandler#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 397
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/platform/exception/GloabalExceptionHandler.java
text:
```scala
package com.example.microSaas.platform.exception;

package com.example.challenge.platform.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Excepti@@onHandler(IllegalArgumentException.class)
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
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ExceptionHandler#