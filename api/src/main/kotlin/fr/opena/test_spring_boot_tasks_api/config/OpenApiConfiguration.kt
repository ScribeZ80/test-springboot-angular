package fr.opena.test_spring_boot_tasks_api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI configuration for the task management API documentation
 */
@Configuration
class OpenApiConfiguration {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("API de Gestion des Tâches")
                    .description("API RESTful pour la gestion des tâches")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Romain Macureau")
                            .email("romain.macureau@gmail.com")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Serveur de développement local")
                )
            )
            .tags(
                listOf(
                    Tag()
                        .name("Tasks")
                        .description("Opérations de gestion des tâches")
                )
            )
    }
} 