package se.lexicon.todo_app.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class MyExceptionHandler {


    // Handle resource not found
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        System.out.println("HandleNoResourceFound: " + ex.getMessage());
        String errorMessage = "Resource not found: " + ex.getMessage();
        return createErrorResponse(HttpStatus.NOT_FOUND, errorMessage);
    }


    // Handle invalid URI parameters (type mismatch)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        System.out.println("HandleMethodArgumentTypeMismatch: " + ex.getMessage());
        String invalidParamMessage = "Parameter '%s' should be of type %s";
        String errorMessage = String.format(invalidParamMessage,
                ex.getName(),
                ex.getRequiredType().getSimpleName());
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);

    }

    // Handle validation errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationViolation(ConstraintViolationException ex) {
        System.out.println("HandleValidationViolation: " + ex.getMessage());
        String[] violations = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .toArray(String[]::new);
        return createErrorResponse(HttpStatus.BAD_REQUEST, violations);
    }

    // Handle runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        System.out.println("HandleRuntimeException: " + ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    // ResponseEntity is a Spring class that represents the entire HTTP response, including status code, headers, and body.
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        System.out.println("HandleGlobalException: " + ex.getMessage());
        String uuid = java.util.UUID.randomUUID().toString().toUpperCase();
        System.err.println("Error ID: " + uuid + " - " + ex.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + uuid);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        System.out.println("HandleAccessDeniedException: " + ex.getMessage());
        ex.printStackTrace();
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        System.out.println("HandleAuthenticationException: " + ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
    }


    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String... errors) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), errors);
        return new ResponseEntity<>(errorResponse, status);
    }


}