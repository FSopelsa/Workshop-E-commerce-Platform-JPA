package se.lexicon.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(example = "Ada")
        String firstName,

        @NotBlank
        @Size(max = 100)
        @Schema(example = "Lovelace")
        String lastName,

        @NotBlank
        @Email
        @Size(max = 150)
        @Schema(example = "ada@example.com")
        String email,

        @NotBlank
        @Size(min = 8, max = 255)
        @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY,
                example = "workshop-test-password", description = "Validated but not stored; no authentication is implemented")
        String password,

        @NotBlank
        @Schema(example = "Test Street 1")
        String street,

        @NotBlank
        @Schema(example = "Stockholm")
        String city,

        @NotBlank
        @Schema(example = "11122")
        String zipCode
) {
}
