package se.lexicon.ecommerce.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "email", "password");
    }

    @Test
    void rejectsEmptyOrderItemsAndNonPositiveQuantities() {
        OrderRequest emptyOrder = new OrderRequest(1L, List.of());
        OrderRequest invalidItemOrder = new OrderRequest(1L, List.of(new OrderItemRequest(2L, 0)));

        assertThat(validator.validate(emptyOrder))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("items");
        assertThat(validator.validate(invalidItemOrder))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("items[0].quantity");
    }
}
