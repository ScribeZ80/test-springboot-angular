package fr.opena.test_spring_boot_tasks_api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Controller for handling home page requests
 */
@Controller
class HomeController {
    
    /**
     * Redirects root path to Swagger UI
     */
    @GetMapping("/")
    fun home(): String {
        return "redirect:/swagger-ui/index.html"
    }
    
    /**
     * Handles favicon.ico requests to avoid 404 errors
     */
    @GetMapping("/favicon.ico")
    fun favicon(): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
} 