package fr.opena.test_spring_boot_tasks_api.controller

import fr.opena.test_spring_boot_tasks_api.dto.CreateTaskRequest
import fr.opena.test_spring_boot_tasks_api.dto.PageResponse
import fr.opena.test_spring_boot_tasks_api.dto.UpdateTaskStatusRequest
import fr.opena.test_spring_boot_tasks_api.model.Task
import fr.opena.test_spring_boot_tasks_api.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for task management
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "API de gestion des tâches")
class TaskController(private val taskService: TaskService) {
    
    /**
     * Retrieves the list of all tasks with pagination
     * GET /api/tasks?page=0&size=10&sort=id,desc
     */
    @Operation(
        summary = "Récupérer toutes les tâches",
        description = "Récupère la liste paginée de toutes les tâches du système"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Liste des tâches récupérée avec succès",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = PageResponse::class))]
        )
    ])
    @GetMapping
    fun getAllTasks(
        @Parameter(
            description = "Paramètres de pagination (page, size, sort)",
            example = "page=0&size=10&sort=id,desc"
        )
        @PageableDefault(size = 10, sort = ["id"]) pageable: Pageable,
        @Parameter(
            name = "completed",
            description = "Filtre optionnel sur le statut de la tâche (true = terminée, false = en cours). Si absent, toutes les tâches sont retournées.",
            required = false,
            example = "true"
        )
        @RequestParam(required = false) completed: Boolean?
    ): ResponseEntity<PageResponse<Task>> {
        val tasks = when (completed) {
            true -> taskService.getCompletedTasks(pageable)
            false -> taskService.getPendingTasks(pageable)
            null -> taskService.getAllTasks(pageable)
        }
        return ResponseEntity.ok(PageResponse.of(tasks))
    }
    
    /**
     * Retrieves pending tasks (not completed) with pagination
     * GET /api/tasks/pending?page=0&size=10&sort=id,desc
     */
    @Operation(
        summary = "Récupérer les tâches en attente",
        description = "Récupère la liste paginée des tâches non terminées (completed = false)"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Liste des tâches en attente récupérée avec succès",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = PageResponse::class))]
        )
    ])
    @GetMapping("/pending")
    fun getPendingTasks(
        @Parameter(
            description = "Paramètres de pagination (page, size, sort)",
            example = "page=0&size=10&sort=id,desc"
        )
        @PageableDefault(size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<PageResponse<Task>> {
        val pendingTasks = taskService.getPendingTasks(pageable)
        return ResponseEntity.ok(PageResponse.of(pendingTasks))
    }
    
    /**
     * Retrieves a task by its ID
     * GET /api/tasks/{id}
     */
    @Operation(
        summary = "Récupérer une tâche par son ID",
        description = "Récupère une tâche spécifique en utilisant son identifiant unique"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Tâche trouvée et récupérée avec succès",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Task::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Tâche non trouvée avec l'ID spécifié",
            content = [Content()]
        )
    ])
    @GetMapping("/{id}")
    fun getTaskById(
        @Parameter(
            description = "Identifiant unique de la tâche",
            example = "1",
            required = true
        )
        @PathVariable id: Long
    ): ResponseEntity<Task> {
        val task = taskService.getTaskById(id)
        return if (task != null) {
            ResponseEntity.ok(task)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Creates a new task
     * POST /api/tasks
     */
    @Operation(
        summary = "Créer une nouvelle tâche",
        description = "Crée une nouvelle tâche avec les informations fournies"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "Tâche créée avec succès",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Task::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Données de la requête invalides",
            content = [Content()]
        )
    ])
    @PostMapping
    fun createTask(
        @Parameter(
            description = "Données de la nouvelle tâche à créer",
            required = true
        )
        @Valid @RequestBody request: CreateTaskRequest
    ): ResponseEntity<Task> {
        val createdTask = taskService.createTask(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }
    
    /**
     * Updates a task status
     * PATCH /api/tasks/{id}/status
     */
    @Operation(
        summary = "Mettre à jour le statut d'une tâche",
        description = "Met à jour le statut de complétion d'une tâche existante"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Statut de la tâche mis à jour avec succès",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Task::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Données de la requête invalides",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Tâche non trouvée avec l'ID spécifié",
            content = [Content()]
        )
    ])
    @PatchMapping("/{id}/status")
    fun updateTaskStatus(
        @Parameter(
            description = "Identifiant unique de la tâche à modifier",
            example = "1",
            required = true
        )
        @PathVariable id: Long,
        @Parameter(
            description = "Nouveau statut de la tâche",
            required = true
        )
        @Valid @RequestBody request: UpdateTaskStatusRequest
    ): ResponseEntity<Task> {
        val updatedTask = taskService.updateTaskStatus(id, request.completed!!)
        return if (updatedTask != null) {
            ResponseEntity.ok(updatedTask)
        } else {
            ResponseEntity.notFound().build()
        }
    }
} 