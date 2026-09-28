package se.lexicon.ecommerce.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    @Test
    void rejectsInvalidCustomerRequestFields() {
        CustomerRequest request = new CustomerRequest(
                "",
                "Lovelace",
                "not-an-email",
                "short",
                "Sveavägen 10",
                "Stockholm",
                "111 57"
        );

        assertThat(validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "email", "password");
    }

    @Test
    void rejectsEmptyOrderItemsAndNonPositiveQuantities() {
        OrderRequest emptyOrder = new OrderRequest(1L, List.of());
        //noinspection DataFlowIssue
        OrderRequest invalidItemOrder = new OrderRequest(1L, List.of(new OrderItemRequest(2L, 0)));

        assertThat(validate(emptyOrder))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("items");
        assertThat(validate(invalidItemOrder))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("items[0].quantity");
    }

    private <T> Set<ConstraintViolation<T>> validate(T value) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(value);
        }
    }
}
