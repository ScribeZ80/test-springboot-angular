package fr.opena.test_spring_boot_tasks_api.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents a task in the application
 */
@Schema(
    name = "Task",
    description = "Représentation d'une tâche dans l'application"
)
data class Task(
    @Schema(
        description = "Identifiant unique de la tâche",
        example = "1",
        required = true
    )
    val id: Long,
    
    @Schema(
        description = "Libellé de la tâche",
        example = "Terminer la documentation API",
        required = true,
        maxLength = 100
    )
    val label: String,
    
    @Schema(
        description = "Description détaillée de la tâche",
        example = "Ajouter la documentation OpenAPI avec Swagger UI pour l'API de gestion des tâches",
        required = true,
        maxLength = 500
    )
    val description: String,
    
    @Schema(
        description = "Indique si la tâche est terminée ou non",
        example = "false",
        required = true,
        defaultValue = "false"
    )
    val completed: Boolean = false
) 