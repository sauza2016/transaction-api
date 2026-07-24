package com.spin.transaction.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Configures the general OpenAPI documentation for the transaction service.
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Defines the metadata displayed in the generated OpenAPI documentation.
     *
     * @return OpenAPI configuration for the transaction service
     */
    @Bean
    OpenAPI transactionServiceOpenAPI() {

        Contact contact = new Contact()
                .name("Arturo Martinez")
                .email("arturo@example.com");

        License license = new License()
                .name("Apache 2.0");

        Info info = new Info()
                .title("Transaction Service API")
                .description("""
                        REST API for creating, processing and retrieving financial
                        transactions through an external transaction provider.
                        """)
                .version("1.0.0")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info);
    }
}