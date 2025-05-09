package com.formos.huub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ***************************************************
 * * Description :
 * * File        : SpringdocConfig
 * * Author      : Hung Tran
 * * Date        : Jun 09, 2024
 * ***************************************************
 **/

@Configuration
public class SpringdocConfig {

    private final String name;
    private final String title;
    private final String apiVersion;
    private final String description;

    public SpringdocConfig(
        @Value("${swagger.sever.name}") String name,
        @Value("${swagger.sever.title}") String title,
        @Value("${swagger.sever.version}") String apiVersion,
        @Value("${swagger.sever.description}") String description
    ) {
        this.name = name;
        this.title = title;
        this.apiVersion = apiVersion;
        this.description = description;
    }

    /**
     * Configure the OpenAPI components.
     *
     * @return Returns fully configure OpenAPI object
     * @see OpenAPI
     */
    @Bean
    public OpenAPI customizeOpenAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList(name))
            .components(
                new Components()
                    .addSecuritySchemes(
                        name,
                        new SecurityScheme()
                            .name(name)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .in(SecurityScheme.In.HEADER)
                    )
            )
            .info(new io.swagger.v3.oas.models.info.Info().title(title).version(apiVersion).description(description));
    }

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder().group("API_v1").pathsToMatch("/api/v1/**").build();
    }

    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder().group("APi_v2").pathsToMatch("/api/v2/**").build();
    }
}
