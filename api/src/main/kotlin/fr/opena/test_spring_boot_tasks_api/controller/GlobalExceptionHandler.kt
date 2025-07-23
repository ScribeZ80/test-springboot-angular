package fr.opena.test_spring_boot_tasks_api.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.time.LocalDateTime

/**
 * Global exception handler for the API
 */
@ControllerAdvice
@Hidden // Hide this controller from OpenAPI documentation
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Data class for error response
     */
    data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String,
        val details: List<String> = emptyList(),
        val exception: String? = null
    )

    /**
     * Handles validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error on ${request.requestURI}: ${ex.message}")
        
        val errors = ex.bindingResult.fieldErrors.map { 
            "${it.field}: ${it.defaultMessage}" 
        }
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Erreurs de validation",
            path = request.requestURI,
            details = errors,
            exception = ex.javaClass.simpleName
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles JSON parsing errors (malformed request body)
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParsingErrors(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("JSON parsing error on ${request.requestURI}: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Format de données invalide",
            path = request.requestURI,
            details = listOf("Le corps de la requête contient des données malformées"),
            exception = ex.javaClass.simpleName
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles method argument type mismatch (e.g., string instead of number)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Argument type error on ${request.requestURI}: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Type de paramètre invalide",
            path = request.requestURI,
            details = listOf("Le paramètre '${ex.name}' doit être de type ${ex.requiredType?.simpleName}"),
            exception = ex.javaClass.simpleName
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Handles 404 Not Found errors
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNotFound(
        ex: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Endpoint not found: ${ex.requestURL}")
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = "Endpoint non trouvé",
            path = request.requestURI,
            details = listOf("L'endpoint ${ex.httpMethod} ${ex.requestURL} n'existe pas"),
            exception = ex.javaClass.simpleName
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Handles SpringDoc/OpenAPI related exceptions
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Runtime error on ${request.requestURI}: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "Erreur interne du serveur",
            path = request.requestURI,
            details = listOf(ex.message ?: "Erreur inconnue"),
            exception = ex.javaClass.simpleName
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    /**
     * Handles general exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled error on ${request.requestURI}: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "Erreur interne du serveur",
            path = request.requestURI,
            details = listOf("Une erreur inattendue s'est produite"),
            exception = ex.javaClass.simpleName
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
} 