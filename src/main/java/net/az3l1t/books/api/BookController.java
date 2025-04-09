package net.az3l1t.books.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.az3l1t.books.model.Book;
import net.az3l1t.books.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "API for managing books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get a paginated list of books", description = "Retrieve books with optional filtering by title, brand, and year")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<Page<Book>> getBooks(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of books per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by book title (optional)") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by book brand (optional)") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by book year (optional)") @RequestParam(required = false) Integer year) {
        Page<Book> books = bookService.getBooks(page, size, title, brand, year);
        return ResponseEntity.ok(books);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new book", description = "Create a book (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "400", description = "Invalid book data")
    })
    public ResponseEntity<Book> createBook(@Parameter(description = "Book details") @RequestBody Book book) {
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.ok(createdBook);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update an existing book", description = "Update a book by ID (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid book data")
    })
    public ResponseEntity<Book> updateBook(
            @Parameter(description = "Book ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated book details") @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a book", description = "Delete a book by ID (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> deleteBook(@Parameter(description = "Book ID", example = "1") @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}