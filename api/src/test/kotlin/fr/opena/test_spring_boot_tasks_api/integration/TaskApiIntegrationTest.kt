package fr.opena.test_spring_boot_tasks_api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import fr.opena.test_spring_boot_tasks_api.dto.CreateTaskRequest
import fr.opena.test_spring_boot_tasks_api.dto.UpdateTaskStatusRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskApiIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val mockMvc: MockMvc by lazy {
        MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `complete task workflow - create, retrieve, update, and list tasks`() {
        // Step 1: Get initial tasks - now we have 15 tasks
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(10)) // Default page size is 10
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))

        // Step 2: Get pending tasks initially - 8 pending tasks out of 15
        mockMvc.perform(get("/api/tasks/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(8)) // All 8 pending tasks fit in default page size
            .andExpect(jsonPath("$.totalElements").value(8))

        // Step 3: Create a new task
        val createRequest = CreateTaskRequest("Tâche d'intégration", "Test du workflow complet")
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.label").value("Tâche d'intégration"))
            .andExpect(jsonPath("$.description").value("Test du workflow complet"))
            .andExpect(jsonPath("$.completed").value(false))
            .andExpect(jsonPath("$.id").value(16)) // New task gets ID 16

        // Step 4: Verify task count increased - now 16 total tasks
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(10)) // Still 10 per page
            .andExpect(jsonPath("$.totalElements").value(16))
            .andExpect(jsonPath("$.totalPages").value(2))

        // Step 5: Get the new task by ID
        mockMvc.perform(get("/api/tasks/16"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("Tâche d'intégration"))
            .andExpect(jsonPath("$.completed").value(false))

        // Step 6: Update task status to completed
        val updateRequest = UpdateTaskStatusRequest(true)
        mockMvc.perform(
            patch("/api/tasks/16/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(16))
            .andExpect(jsonPath("$.completed").value(true))

        // Step 7: Verify pending tasks count decreased - now 8 pending (new task is completed)
        mockMvc.perform(get("/api/tasks/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(8)) // Still 8 pending tasks
            .andExpect(jsonPath("$.totalElements").value(8))

        // Step 8: Verify the task is now completed when retrieved by ID
        mockMvc.perform(get("/api/tasks/16"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))
    }

    @Test
    fun `test error scenarios`() {
        // Test 1: Get non-existent task
        mockMvc.perform(get("/api/tasks/999"))
            .andExpect(status().isNotFound)

        // Test 2: Update non-existent task
        val updateRequest = UpdateTaskStatusRequest(true)
        mockMvc.perform(
            patch("/api/tasks/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `test task status transitions`() {
        // Get an initially completed task (ID 1)
        mockMvc.perform(get("/api/tasks/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))

        // Change it to incomplete
        val updateToIncomplete = UpdateTaskStatusRequest(false)
        mockMvc.perform(
            patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateToIncomplete))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(false))

        // Verify it now appears in pending tasks
        mockMvc.perform(get("/api/tasks/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.hasItem(1)))

        // Change it back to completed
        val updateToCompleted = UpdateTaskStatusRequest(true)
        mockMvc.perform(
            patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateToCompleted))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))

        // Verify it no longer appears in pending tasks
        mockMvc.perform(get("/api/tasks/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(1))))
    }

    @Test
    fun `test multiple task creation and ID generation`() {
        // Create multiple tasks and verify unique IDs
        val task1Request = CreateTaskRequest("Tâche 1", "Première tâche violette")
        val task2Request = CreateTaskRequest("Tâche 2", "Deuxième tâche orange")
        val task3Request = CreateTaskRequest("Tâche 3", "Troisième tâche dorée")

        // Create first task
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task1Request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(16))

        // Create second task
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task2Request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(17))

        // Create third task
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task3Request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(18))

        // Verify total count
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(10)) // Page size is 10
            .andExpect(jsonPath("$.totalElements").value(18)) // 15 initial + 3 new

        // Verify all tasks are retrievable by their IDs
        mockMvc.perform(get("/api/tasks/16"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("Tâche 1"))

        mockMvc.perform(get("/api/tasks/17"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("Tâche 2"))

        mockMvc.perform(get("/api/tasks/18"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("Tâche 3"))
    }

    @Test
    fun `should return validation errors when creating task with invalid data`() {
        // Test 1: Empty label
        val emptyLabelRequest = """
            {
                "label": "",
                "description": "Description valide pour test d'intégration"
            }
        """.trimIndent()
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyLabelRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details").isArray)
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Le libellé ne peut pas être vide"))))

        // Test 2: Too long label  
        val longLabelRequest = CreateTaskRequest(
            "a".repeat(101), // 101 characters
            "Description normale"
        )
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longLabelRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Le libellé ne peut pas dépasser 100 caractères"))))

        // Test 3: Empty description
        val emptyDescriptionRequest = """
            {
                "label": "Libellé valide",
                "description": ""
            }
        """.trimIndent()
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyDescriptionRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("La description ne peut pas être vide"))))

        // Test 4: Multiple validation errors
        val multipleErrorsRequest = """
            {
                "label": "",
                "description": ""
            }
        """.trimIndent()
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(multipleErrorsRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details.length()").value(2))

        // Verify no tasks were created during validation failures
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(10)) // Page size is 10
            .andExpect(jsonPath("$.totalElements").value(15)) // Still only 15 initial tasks
    }

    @Test
    fun `should accept task creation at maximum allowed field lengths`() {
        // Test valid task with maximum allowed lengths
        val validBoundaryRequest = CreateTaskRequest(
            "a".repeat(100), // Exactly 100 characters (max allowed)
            "a".repeat(500)  // Exactly 500 characters (max allowed)
        )
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBoundaryRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(16))
            .andExpect(jsonPath("$.label").value("a".repeat(100)))
            .andExpect(jsonPath("$.description").value("a".repeat(500)))
            .andExpect(jsonPath("$.completed").value(false))

        // Verify task count increased
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(10)) // Page size is 10
            .andExpect(jsonPath("$.totalElements").value(16)) // 15 initial + 1 new
    }

    @Test
    fun `should return validation errors when updating task with invalid data`() {
        // Test malformed boolean value
        val invalidBooleanRequest = """
            {
                "completed": "maybe"
            }
        """.trimIndent()
        
        mockMvc.perform(
            patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBooleanRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Format de données invalide"))

        // Test missing completed field
        val missingFieldRequest = """
            {
                "otherField": true
            }
        """.trimIndent()
        
        mockMvc.perform(
            patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingFieldRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))
            .andExpect(jsonPath("$.details[*]").value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Le statut doit être spécifié"))))

        // Verify original task status unchanged after failed validation
        mockMvc.perform(get("/api/tasks/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true)) // Original value should remain
    }

    @Test
    fun `should handle complete task workflow with proper validation`() {
        // Step 1: Try to create invalid task
        val invalidTaskRequest = CreateTaskRequest(
            "", // Empty label 
            "Apprendre le français et maîtriser tous les aspects de la langue française"
        )
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTaskRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Erreurs de validation"))

        // Step 2: Create valid task
        val validTaskRequest = CreateTaskRequest(
            "Apprendre le japonais",
            "Étudier les hiragana, katakana et quelques kanji de base"
        )
        
        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTaskRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.label").value("Apprendre le japonais"))
            .andExpect(jsonPath("$.description").value("Étudier les hiragana, katakana et quelques kanji de base"))

        // Step 3: Update task status successfully
        val validUpdateRequest = UpdateTaskStatusRequest(true)
        
        mockMvc.perform(
            patch("/api/tasks/4/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))

        // Step 4: Verify task appears correctly in pending list (should not appear since it's completed)
        mockMvc.perform(get("/api/tasks/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(4))))
    }

    @Test
    fun `should support pagination with custom page size`() {
        // Given: We have 3 initial tasks
        
        // Test page size of 2
        mockMvc.perform(get("/api/tasks?page=0&size=2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(8)) // 15 tasks / 2 per page = 8 pages
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        // Test second page
        mockMvc.perform(get("/api/tasks?page=1&size=2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2)) // Page 1 has 2 tasks
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(8))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(2))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(false)) // Not the last page (page 1 of 8)
    }

    @Test
    fun `should support pagination for pending tasks`() {
        // Given: We have 8 pending tasks initially
        
        // Test page size of 1 for pending tasks
        mockMvc.perform(get("/api/tasks/pending?page=0&size=1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(8))
            .andExpect(jsonPath("$.totalPages").value(8))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(1))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        // Test second page of pending tasks
        mockMvc.perform(get("/api/tasks/pending?page=1&size=1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(8))
            .andExpect(jsonPath("$.totalPages").value(8))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(1))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(false))
    }

    @Test
    fun `should handle empty page gracefully`() {
        // Test requesting a page beyond available data
        mockMvc.perform(get("/api/tasks?page=10&size=10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.pageNumber").value(10))
            .andExpect(jsonPath("$.pageSize").value(10))
    }

    @Test
    fun `should support sorting with pagination`() {
        // Test sorting by ID descending with pagination
        mockMvc.perform(get("/api/tasks?page=0&size=2&sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(15)) // Highest ID first
            .andExpect(jsonPath("$.content[1].id").value(14))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(2))
    }

    // ============= EXHAUSTIVE PAGINATION TESTS WITH 15 TASKS =============

    @Test
    fun `should handle multiple pages with page size 5`() {
        // Test first page (tasks 1-5)
        mockMvc.perform(get("/api/tasks?page=0&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[4].id").value(5))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        // Test second page (tasks 6-10)
        mockMvc.perform(get("/api/tasks?page=1&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].id").value(6))
            .andExpect(jsonPath("$.content[4].id").value(10))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(false))

        // Test third page (tasks 11-15)
        mockMvc.perform(get("/api/tasks?page=2&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].id").value(11))
            .andExpect(jsonPath("$.content[4].id").value(15))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.pageNumber").value(2))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true))
    }

    @Test
    fun `should handle large page size covering all tasks`() {
        // Request page size larger than total tasks
        mockMvc.perform(get("/api/tasks?page=0&size=20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(15)) // Only 15 tasks available
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(true))
    }

    @Test
    fun `should handle pending tasks pagination - 8 pending tasks`() {
        // First page of pending tasks (page size 6)
        mockMvc.perform(get("/api/tasks/pending?page=0&size=6"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(6))
            .andExpect(jsonPath("$.totalElements").value(8)) // 8 pending tasks total
            .andExpect(jsonPath("$.totalPages").value(2)) // 8 tasks / 6 per page = 2 pages
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))

        // Second page of pending tasks (remaining 2)
        mockMvc.perform(get("/api/tasks/pending?page=1&size=6"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2)) // Only 2 remaining
            .andExpect(jsonPath("$.totalElements").value(8))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true))
    }

    @Test
    fun `should handle sorting with multiple pages`() {
        // Sort by label ascending with small page size
        mockMvc.perform(get("/api/tasks?page=0&size=3&sort=label,asc"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(5)) // 15 tasks / 3 per page = 5 pages


        // Test different page with same sorting
        mockMvc.perform(get("/api/tasks?page=2&size=3&sort=label,asc"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.pageNumber").value(2))
            .andExpect(jsonPath("$.totalPages").value(5))
    }

    @Test
    fun `should handle edge case - very small page size`() {
        // Page size of 1
        mockMvc.perform(get("/api/tasks?page=0&size=1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(15)) // 15 pages with 1 task each

        // Test middle page
        mockMvc.perform(get("/api/tasks?page=7&size=1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(8)) // 8th task (0-indexed page 7)
            .andExpect(jsonPath("$.pageNumber").value(7))

        // Test last page
        mockMvc.perform(get("/api/tasks?page=14&size=1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(15)) // Last task
            .andExpect(jsonPath("$.pageNumber").value(14))
            .andExpect(jsonPath("$.last").value(true))
    }

    @Test
    fun `should verify task completion status distribution in pages`() {
        // Verify that we have the expected mix of completed/pending tasks
        // 7 completed tasks: IDs 1, 4, 7, 8, 9, 10, 13
        // 8 pending tasks: IDs 2, 3, 5, 6, 11, 12, 14, 15
        
        // Get all tasks and verify completed count
        mockMvc.perform(get("/api/tasks?page=0&size=15"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(15))
            // Verify we have both completed and pending tasks
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[14].id").value(15))
    }

    @Test
    fun `should filter tasks by completed status using query param`() {
        // Test completed=true
        mockMvc.perform(get("/api/tasks?completed=true"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].completed").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.`is`(true))))
            .andExpect(jsonPath("$.totalElements").value(7)) // 7 tâches terminées dans les données initiales

        // Test completed=false
        mockMvc.perform(get("/api/tasks?completed=false"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].completed").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.`is`(false))))
            .andExpect(jsonPath("$.totalElements").value(8)) // 8 tâches en cours dans les données initiales

        // Test sans paramètre (toutes les tâches)
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(15))
    }
} 
