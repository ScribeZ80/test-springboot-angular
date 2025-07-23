package fr.opena.test_spring_boot_tasks_api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

/**
 * DTO for updating task status
 */
@Schema(
    name = "UpdateTaskStatusRequest",
    description = "Requête pour mettre à jour le statut d'une tâche"
)
data class UpdateTaskStatusRequest(
    @field:NotNull(message = "Le statut doit être spécifié")
    @Schema(
        description = "Nouveau statut de la tâche (true = terminée, false = en cours)",
        example = "true",
        required = true
    )
    val completed: Boolean?
) 