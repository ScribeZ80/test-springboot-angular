package fr.opena.test_spring_boot_tasks_api.service

import fr.opena.test_spring_boot_tasks_api.dto.CreateTaskRequest
import fr.opena.test_spring_boot_tasks_api.model.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

/**
 * Service for task management
 */
@Service
class TaskService {
    
    private val taskIdCounter = AtomicLong(16) // Updated to accommodate 15 tasks
    
    private val tasks = mutableListOf(
        Task(1L, "Boire une bière", "Retrouver les potes et boire une bière (minimum)", true),
        Task(2L, "Acheter une moto", "Et en faire", false),
        Task(3L, "Sauter en parachute", "Plusieurs fois", false),
        Task(4L, "Apprendre le russe", "Mais sans aller en Russie", true),
        Task(5L, "Faire un potager", "Intelligemment", false),
        Task(6L, "Lire un livre", "Celui qui est sur la table de chevet depuis 6 mois", false),
        Task(7L, "Cuisiner des pâtes", "Préparer des pâtes carbonara authentiques", true),
        Task(8L, "Nettoyer la maison", "Grand ménage de printemps", true),
        Task(9L, "Appeler les parents", "Prendre des nouvelles et discuter", true),
        Task(10L, "Faire les courses", "Acheter des légumes et fruits frais", true),
        Task(11L, "Regarder un film", "Voir le dernier film de science-fiction", false),
        Task(12L, "Faire du sport", "Aller courir 7km mais pas au delà de 20°C", false),
        Task(13L, "Réparer le vélo", "Changer la chaîne et gonfler les pneus", true),
        Task(14L, "Organiser le bureau", "Ranger et trier tous les documents", false),
        Task(15L, "Planter des fleurs", "Semer des capucines dans le jardin", false)
    )
    
        /**
     * Retrieves all tasks with pagination
     */
    fun getAllTasks(pageable: Pageable): Page<Task> {
        val allTasks = tasks.toList()
        return createPage(allTasks, pageable)
    }

    /**
     * Retrieves pending tasks (not completed) with pagination
     */
    fun getPendingTasks(pageable: Pageable): Page<Task> {
        val pendingTasks = tasks.filter { !it.completed }
        return createPage(pendingTasks, pageable)
    }

    /**
     * Retrieves completed tasks (completed = true) with pagination
     */
    fun getCompletedTasks(pageable: Pageable): Page<Task> {
        val completedTasks = tasks.filter { it.completed }
        return createPage(completedTasks, pageable)
    }

    /**
     * Creates a paginated result from a list of tasks
     */
    private fun createPage(allTasks: List<Task>, pageable: Pageable): Page<Task> {
        // Apply sorting if specified
        val sortedTasks = if (pageable.sort.isSorted) {
            applySorting(allTasks, pageable)
        } else {
            allTasks
        }
        
        val startIndex = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sortedTasks.size)
        val endIndex = (startIndex + pageable.pageSize).coerceAtMost(sortedTasks.size)
        
        val pageContent = if (startIndex < sortedTasks.size) {
            sortedTasks.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PageImpl(pageContent, pageable, sortedTasks.size.toLong())
    }

    /**
     * Applies sorting to a list of tasks based on Pageable sort parameters
     */
    private fun applySorting(tasks: List<Task>, pageable: Pageable): List<Task> {
        var sortedTasks = tasks
        
        for (order in pageable.sort) {
            val comparator: Comparator<Task> = when (order.property) {
                "id" -> Comparator { t1, t2 -> t1.id.compareTo(t2.id) }
                "label" -> Comparator { t1, t2 -> t1.label.compareTo(t2.label) }
                "description" -> Comparator { t1, t2 -> t1.description.compareTo(t2.description) }
                "completed" -> Comparator { t1, t2 -> t1.completed.compareTo(t2.completed) }
                else -> Comparator { _, _ -> 0 } // No sorting for unknown properties
            }
            
            sortedTasks = if (order.isDescending) {
                sortedTasks.sortedWith(comparator.reversed())
            } else {
                sortedTasks.sortedWith(comparator)
            }
        }
        
        return sortedTasks
    }
    
    /**
     * Retrieves a task by its ID
     */
    fun getTaskById(id: Long): Task? = tasks.find { it.id == id }
    
    /**
     * Creates a new task
     */
    fun createTask(request: CreateTaskRequest): Task {
        val newTask = Task(
            id = taskIdCounter.getAndIncrement(),
            label = request.label,
            description = request.description
        )
        tasks.add(newTask)
        return newTask
    }
    
    /**
     * Updates a task status
     */
    fun updateTaskStatus(id: Long, completed: Boolean): Task? {
        val taskIndex = tasks.indexOfFirst { it.id == id }
        return if (taskIndex != -1) {
            val updatedTask = tasks[taskIndex].copy(completed = completed)
            tasks[taskIndex] = updatedTask
            updatedTask
        } else {
            null
        }
    }
} 