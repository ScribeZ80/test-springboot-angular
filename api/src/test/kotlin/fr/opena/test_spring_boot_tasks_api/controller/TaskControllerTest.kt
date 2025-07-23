package fr.opena.test_spring_boot_tasks_api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import fr.opena.test_spring_boot_tasks_api.dto.CreateTaskRequest
import fr.opena.test_spring_boot_tasks_api.dto.UpdateTaskStatusRequest
import fr.opena.test_spring_boot_tasks_api.model.Task
import fr.opena.test_spring_boot_tasks_api.service.TaskService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TaskController::class)
class TaskControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val task1 = Task(1L, "Boire une bière", "Retrouver les potes et boire une bière (minimum)", true)
    private val task2 = Task(2L, "Acheter une moto", "Et en faire", false)
    private val task3 = Task(3L, "Sauter en parachute", "Plusieurs fois", false)
    private val task4 = Task(4L, "Apprendre le russe", "Mais sans aller en Russie", true)
    private val task5 = Task(5L, "Faire un potager", "Intelligemment", false)
    private val task6 = Task(6L, "Lire un livre", "Celui qui est sur la table de chevet depuis 6 mois", false)
    private val task7 = Task(7L, "Cuisiner des pâtes", "Préparer des pâtes carbonara authentiques", true)
    private val task8 = Task(8L, "Nettoyer la maison", "Grand ménage de printemps", true)
    private val task9 = Task(9L, "Appeler les parents", "Prendre des nouvelles et discuter", true)
    private val task10 = Task(10L, "Faire les courses", "Acheter des légumes et fruits frais", true)
    private val task11 = Task(11L, "Regarder un film", "Voir le dernier film de science-fiction", false)
    private val task12 = Task(12L, "Faire du sport", "Aller courir 7km mais pas au delà de 20°C", false)
    private val task13 = Task(13L, "Réparer le vélo", "Changer la chaîne et gonfler les pneus", true)
    private val task14 = Task(14L, "Organiser le bureau", "Ranger et trier tous les documents", false)
    private val task15 = Task(15L, "Planter des fleurs", "Semer des capucines dans le jardin", false)

    private val allTasks = listOf(task1, task2, task3, task4, task5, task6, task7, task8, task9, task10, task11, task12, task13, task14, task15)
    private val pendingTasks = allTasks.filter { !it.completed } // 8 pending tasks

    @Test
    fun `getAllTasks should return paginated tasks with default pagination`() {
        // Given - First 10 tasks (default page size)
        val firstPageTasks = allTasks.take(10)
        val page = PageImpl(firstPageTasks, PageRequest.of(0, 10), 15)
        val pageRequest = PageRequest.of(0, 10, Sort.by("id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].label").value("Boire une bière"))
            .andExpect(jsonPath("$.content[0].description").value("Retrouver les potes et boire une bière (minimum)"))
            .andExpect(jsonPath("$.content[0].completed").value(true))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].completed").value(false))
            .andExpect(jsonPath("$.content[9].id").value(10))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getAllTasks should return empty page when no tasks exist`() {
        // Given
        val emptyPage = PageImpl(emptyList<Task>(), PageRequest.of(0, 10), 0)
        val pageRequest = PageRequest.of(0, 10, Sort.by("id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(emptyPage)

        // When & Then
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getPendingTasks should return paginated incomplete tasks with 200 status`() {
        // Given
        val pendingTasksPage = PageImpl(pendingTasks, PageRequest.of(0, 10), 8)
        val pageRequest = PageRequest.of(0, 10, Sort.by("id"))
        given(taskService.getPendingTasks(pageRequest)).willReturn(pendingTasksPage)

        // When & Then
        mockMvc.perform(get("/api/tasks/pending"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(8))
            .andExpect(jsonPath("$.content[0].id").value(2))
            .andExpect(jsonPath("$.content[0].completed").value(false))
            .andExpect(jsonPath("$.content[7].id").value(15))
            .andExpect(jsonPath("$.totalElements").value(8))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))

        verify(taskService).getPendingTasks(pageRequest)
    }

    @Test
    fun `getTaskById should return task with 200 status when task exists`() {
        // Given
        val taskId = 1L
        given(taskService.getTaskById(taskId)).willReturn(task1)

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", taskId))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.label").value("Boire une bière"))
            .andExpect(jsonPath("$.description").value("Retrouver les potes et boire une bière (minimum)"))
            .andExpect(jsonPath("$.completed").value(true))

        verify(taskService).getTaskById(taskId)
    }

    @Test
    fun `getTaskById should return 404 status when task does not exist`() {
        // Given
        val taskId = 999L
        given(taskService.getTaskById(taskId)).willReturn(null)

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", taskId))
            .andExpect(status().isNotFound)

        verify(taskService).getTaskById(taskId)
    }

    @Test
    fun `createTask should create new task and return 201 status`() {
        // Given
        val createRequest = CreateTaskRequest("Nouvelle tâche", "Nouvelle tâche rouge")
        val createdTask = Task(4L, "Nouvelle tâche", "Nouvelle tâche rouge", false)
        given(taskService.createTask(createRequest)).willReturn(createdTask)

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.label").value("Nouvelle tâche"))
            .andExpect(jsonPath("$.description").value("Nouvelle tâche rouge"))
            .andExpect(jsonPath("$.completed").value(false))

        verify(taskService).createTask(createRequest)
    }

    @Test
    fun `updateTaskStatus should update task and return 200 status when task exists`() {
        // Given
        val taskId = 2L
        val updateRequest = UpdateTaskStatusRequest(true)
        val updatedTask = task2.copy(completed = true)
        given(taskService.updateTaskStatus(taskId, true)).willReturn(updatedTask)

        // When & Then
        mockMvc.perform(
            patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.label").value("Acheter une moto"))
            .andExpect(jsonPath("$.completed").value(true))

        verify(taskService).updateTaskStatus(taskId, true)
    }

    @Test
    fun `updateTaskStatus should return 404 status when task does not exist`() {
        // Given
        val taskId = 999L
        val updateRequest = UpdateTaskStatusRequest(true)
        given(taskService.updateTaskStatus(taskId, true)).willReturn(null)

        // When & Then
        mockMvc.perform(
            patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)

        verify(taskService).updateTaskStatus(taskId, true)
    }

    @Test
    fun `updateTaskStatus should handle status change from true to false`() {
        // Given
        val taskId = 1L
        val updateRequest = UpdateTaskStatusRequest(false)
        val updatedTask = task1.copy(completed = false)
        given(taskService.updateTaskStatus(taskId, false)).willReturn(updatedTask)

        // When & Then
        mockMvc.perform(
            patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.completed").value(false))

        verify(taskService).updateTaskStatus(taskId, false)
    }

    @Test
    fun `createTask should return 400 when label is blank`() {
        // Given
        val invalidRequest = """
            {
                "label": "",
                "description": "Description valide"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Le libellé ne peut pas être vide"))))
    }

    @Test
    fun `createTask should return 400 when description is blank`() {
        // Given
        val invalidRequest = """
            {
                "label": "Libellé valide",
                "description": ""
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La description ne peut pas être vide"))))
    }

    @Test
    fun `createTask should return 400 when label is too long`() {
        // Given
        val longLabel = "a".repeat(101) // 101 characters, exceeds max of 100
        val invalidRequest = """
            {
                "label": "$longLabel",
                "description": "Description valide"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Le libellé ne peut pas dépasser 100 caractères"))))
    }

    @Test
    fun `createTask should return 400 when description is too long`() {
        // Given
        val longDescription = "a".repeat(501) // 501 characters, exceeds max of 500
        val invalidRequest = """
            {
                "label": "Libellé valide",
                "description": "$longDescription"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La description ne peut pas dépasser 500 caractères"))))
    }

    @Test
    fun `createTask should return 400 when both label and description are invalid`() {
        // Given
        val invalidRequest = """
            {
                "label": "",
                "description": ""
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details.length()").value(2))
    }

    @Test
    fun `createTask should accept valid data at boundary limits`() {
        // Given
        val maxLengthLabel = "a".repeat(100) // Exactly 100 characters
        val maxLengthDescription = "a".repeat(500) // Exactly 500 characters
        val createRequest = CreateTaskRequest(maxLengthLabel, maxLengthDescription)
        val createdTask = Task(4L, maxLengthLabel, maxLengthDescription, false)
        given(taskService.createTask(createRequest)).willReturn(createdTask)

        // When & Then
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.label").value(maxLengthLabel))
            .andExpect(jsonPath("$.description").value(maxLengthDescription))

        verify(taskService).createTask(createRequest)
    }

    @Test
    fun `updateTaskStatus should return 400 when request body is malformed`() {
        // Given
        val taskId = 1L
        val malformedRequest = """
            {
                "completed": "not_a_boolean"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Format de données invalide"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("malformées"))))
    }

    @Test
    fun `getAllTasks should support custom pagination parameters`() {
        // Given - Second page contains only 5 remaining tasks
        val secondPageTasks = allTasks.drop(10) // Skip first 10 tasks, get remaining 5
        val page = PageImpl(secondPageTasks, PageRequest.of(1, 10), 15) // Second page, 10 items per page, 15 total
        val pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks?page=1&size=10&sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getPendingTasks should support custom pagination parameters`() {
        // Given
        val pendingTasksPage = PageImpl(pendingTasks, PageRequest.of(0, 10), 8) // First page, 10 items per page, 8 total
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "label"))
        given(taskService.getPendingTasks(pageRequest)).willReturn(pendingTasksPage)

        // When & Then
        mockMvc.perform(get("/api/tasks/pending?page=0&size=10&sort=label,asc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(8))
            .andExpect(jsonPath("$.content[0].id").value(2))
            .andExpect(jsonPath("$.totalElements").value(8))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(true))

        verify(taskService).getPendingTasks(pageRequest)
    }

    @Test
    fun `getAllTasks should return last page when page number exceeds available pages`() {
        // Given - only 1 page available but requesting page 2
        val page = PageImpl(emptyList<Task>(), PageRequest.of(2, 10), 1) // Page 2, but only 1 item total
        val pageRequest = PageRequest.of(2, 10, Sort.by("id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks?page=2&size=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.pageNumber").value(2))
            .andExpect(jsonPath("$.pageSize").value(10))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getAllTasks should handle second page correctly`() {
        // Given - Second page with 5 remaining tasks
        val secondPageTasks = allTasks.drop(10) // Tasks 11-15
        val page = PageImpl(secondPageTasks, PageRequest.of(1, 10), 15)
        val pageRequest = PageRequest.of(1, 10, Sort.by("id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks?page=1&size=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].id").value(11))
            .andExpect(jsonPath("$.content[4].id").value(15))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getAllTasks should handle small page size with multiple pages`() {
        // Given - First page with page size 5
        val firstPageTasks = allTasks.take(5) // Tasks 1-5
        val page = PageImpl(firstPageTasks, PageRequest.of(0, 5), 15)
        val pageRequest = PageRequest.of(0, 5, Sort.by("id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks?page=0&size=5"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[4].id").value(5))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(5))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getAllTasks should handle middle page correctly`() {
        // Given - Second page of 3 with page size 5 (tasks 6-10)
        val middlePageTasks = allTasks.drop(5).take(5) // Tasks 6-10
        val page = PageImpl(middlePageTasks, PageRequest.of(1, 5), 15)
        val pageRequest = PageRequest.of(1, 5, Sort.by("id"))
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks?page=1&size=5"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].id").value(6))
            .andExpect(jsonPath("$.content[4].id").value(10))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(5))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(false))

        verify(taskService).getAllTasks(pageRequest)
    }

    @Test
    fun `getPendingTasks should handle pagination with 8 pending tasks`() {
        // Given - First page of pending tasks (page size 5)
        val firstPagePending = pendingTasks.take(5)
        val page = PageImpl(firstPagePending, PageRequest.of(0, 5), 8)
        val pageRequest = PageRequest.of(0, 5, Sort.by("id"))
        given(taskService.getPendingTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks/pending?page=0&size=5"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].completed").value(false))
            .andExpect(jsonPath("$.content[4].completed").value(false))
            .andExpect(jsonPath("$.totalElements").value(8))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(5))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        verify(taskService).getPendingTasks(pageRequest)
    }

    @Test
    fun `getAllTasks should handle sorting with pagination`() {
        // Given - Tasks sorted by label in descending order
        val sortedTasks = allTasks.sortedByDescending { it.label }.take(10)
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "label"))
        val page = PageImpl(sortedTasks, pageRequest, 15)
        given(taskService.getAllTasks(pageRequest)).willReturn(page)

        // When & Then
        mockMvc.perform(get("/api/tasks?page=0&size=10&sort=label,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))

        verify(taskService).getAllTasks(pageRequest)
    }
} 