package com.epam.gymcrm.api;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ForbiddenException;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception,
                                              HttpServletRequest request) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return problem(HttpStatus.BAD_REQUEST, detail, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> badRequest(IllegalArgumentException exception,
                                             HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> unauthorized(AuthenticationException exception,
                                               HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ProblemDetail> forbidden(ForbiddenException exception,
                                            HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException exception,
                                           HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ProblemDetail> conflict(RuntimeException exception,
                                           HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected request failure", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private static ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String detail,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(request.getRequestURI()));
        String transactionId = MDC.get(TransactionLoggingFilter.TRANSACTION_ID);
        if (transactionId != null) {
            problem.setProperty("transactionId", transactionId);
        }
        return ResponseEntity.status(status).body(problem);
    }
}
