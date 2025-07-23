package fr.opena.test_spring_boot_tasks_api.dto

import org.springframework.data.domain.Page

/**
 * Custom pagination response DTO to avoid PageImpl serialization issues
 */
data class PageResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        /**
         * Factory method to create PageResponse from Spring Data Page
         */
        fun <T> of(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                pageNumber = page.number,
                pageSize = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious()
            )
        }
    }
} 