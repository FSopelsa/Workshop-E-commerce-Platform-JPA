package se.lexicon.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
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
        return problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", detail);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleMalformedRequest(Exception exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Malformed request", "Request values could not be read");
    }

    private ProblemDetail problemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
