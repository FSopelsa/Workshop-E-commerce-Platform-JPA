package se.lexicon.ecommerce.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException exception) {
        return problemDetail(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Resource conflict", exception.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail handleInvalidRequest(InvalidRequestException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        String detail = exception.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String message = Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid");
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + message;
                    }
                    return message;
                })
                .distinct()
                .collect(Collectors.joining("; "));
        if (detail.isBlank()) {
            detail = "Request validation failed";
        }
        ProblemDetail problem = problemDetail(status, "Validation failed", detail);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        return handleExceptionInternal(exception, malformedRequest(status), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        return handleExceptionInternal(exception, malformedRequest(status), headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        logger.error("Unexpected error while processing an API request", exception);
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred. Please try again later.");
    }

    private ProblemDetail malformedRequest(HttpStatusCode status) {
        return problemDetail(status, "Malformed request", "Request values could not be read");
    }

    private ProblemDetail problemDetail(HttpStatusCode status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
