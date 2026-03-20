package org.formation.web;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleStock(IllegalStateException e) {
        Span.current()
                .setStatus(StatusCode.ERROR, e.getMessage())
                .recordException(e);
        return Map.of("error", e.getMessage(), "code", "OUT_OF_STOCK");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(RuntimeException e) {
        Span.current()
                .setStatus(StatusCode.ERROR, e.getMessage())
                .recordException(e);
        return Map.of("error", e.getMessage(), "code", "NOT_FOUND");
    }
}
