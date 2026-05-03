package com.toDo.tarefas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados gerais expostos no cabeçalho do Swagger UI e na raiz do
 * {@code /v3/api-docs}. Sem este bean, o springdoc preenche um título
 * genérico ("OpenAPI definition").
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tarefasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Tarefas")
                        .description("MVP de API REST para gerenciar tarefas (CRUD).")
                        .version("1.0.0"));
    }
}
