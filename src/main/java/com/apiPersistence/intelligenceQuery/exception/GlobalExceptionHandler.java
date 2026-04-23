package com.apiPersistence.intelligenceQuery.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class); // add this


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(422).body(Map.of(
                "status", "error",
                "message", "Invalid parameter type: " + ex.getName()
        ));
    }

    @ExceptionHandler(UninterpretableQueryException.class)
    public ResponseEntity<Map<String, String>> handleUninterpretable(UninterpretableQueryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", "error",
                "message", "Unable to interpret query"
        ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: ", ex);  // add this line
        return ResponseEntity.status(500).body(
                Map.of("status", "error", "message", "Server failure")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        if ("Invalid query parameters".equals(ex.getMessage())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid query parameters"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", ex.getMessage() == null ? "Bad Request" : ex.getMessage()
        ));
    }
}
