package se.lexicon.ecommerce.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsMissingResourcesToNotFoundProblemDetails() {
        ProblemDetail problem = handler.handleResourceNotFound(
                new ResourceNotFoundException("product not found: 42")
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Resource not found");
        assertThat(problem.getDetail()).isEqualTo("product not found: 42");
    }

    @Test
    void mapsDuplicateResourcesToConflictProblemDetails() {
        ProblemDetail problem = handler.handleDuplicateResource(
                new DuplicateResourceException("customer email is already registered")
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Resource conflict");
        assertThat(problem.getDetail()).isEqualTo("customer email is already registered");
    }
}
