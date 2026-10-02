package se.lexicon.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenApi() {
        ObjectSchema problemDetailSchema = new ObjectSchema();
        problemDetailSchema.setDescription("HTTP error details. An omitted type means about:blank.");
        problemDetailSchema.addProperty("type", new StringSchema().format("uri-reference"));
        problemDetailSchema.addProperty("title", new StringSchema());
        problemDetailSchema.addProperty("status", new IntegerSchema().format("int32"));
        problemDetailSchema.addProperty("detail", new StringSchema());
        problemDetailSchema.addProperty("instance", new StringSchema().format("uri-reference"));
        problemDetailSchema.setRequired(List.of("title", "status", "detail", "instance"));

        Components components = new Components()
                .addSchemas("ProblemDetail", problemDetailSchema)
                .addResponses("BadRequest", problemResponse("Invalid request fields, values, or service rules"))
                .addResponses("NotFound", problemResponse("The requested or referenced resource does not exist"))
                .addResponses("Conflict", problemResponse("The email, category name, or promotion code already exists"))
                .addResponses("InternalServerError", problemResponse("Unexpected failure; internal details are not exposed"));

        return new OpenAPI()
                .info(new Info()
                        .title("Workshop E-commerce API")
                        .version("v1")
                        .description("Customer, catalog, promotion, and order endpoints for the JPA workshop. "
                                + "Use the returned resource IDs in later requests. "
                                + "Customer passwords are validated but not stored; this API has no authentication."))
                .components(components);
    }

    private ApiResponse problemResponse(String description) {
        return new ApiResponse().description(description)
                .content(new Content().addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))));
    }
}
