package fr.opena.test_spring_boot_tasks_api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for creating a new task
 */
@Schema(
    name = "CreateTaskRequest",
    description = "Requête pour créer une nouvelle tâche"
)
data class CreateTaskRequest(
    @field:NotBlank(message = "Le libellé ne peut pas être vide")
    @field:Size(max = 100, message = "Le libellé ne peut pas dépasser 100 caractères")
    @Schema(
        description = "Libellé de la tâche à créer",
        example = "Terminer la documentation API",
        required = true,
        maxLength = 100
    )
    val label: String,
    
    @field:NotBlank(message = "La description ne peut pas être vide")
    @field:Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Schema(
        description = "Description détaillée de la tâche à créer",
        example = "Ajouter la documentation OpenAPI avec Swagger UI pour l'API de gestion des tâches",
        required = true,
        maxLength = 500
    )
    val description: String
) 