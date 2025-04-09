package net.az3l1t.books.service;

import lombok.extern.slf4j.Slf4j;
import net.az3l1t.books.exception.BookWasNotFoundException;
import net.az3l1t.books.model.Book;
import net.az3l1t.books.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final RedisCacheService redisCacheService;

    public BookService(BookRepository bookRepository, RedisCacheService redisCacheService) {
        this.bookRepository = bookRepository;
        this.redisCacheService = redisCacheService;
    }

    @Transactional
    public Page<Book> getBooks(int page, int size, String title, String brand, Integer year) {
        String cacheKey = generateCacheKey(page, size, title, brand, year);
        log.debug("Fetching books with cacheKey: {}", cacheKey);

        Page<Book> cachedBooks = (Page<Book>) redisCacheService.getFromCache(cacheKey);
        if (cachedBooks != null) {
            log.info("Books got from cache for key: {}", cacheKey);
            return cachedBooks;
        }

        log.debug("Cache miss, check database for books with page={}, size={}, title={}, brand={}, year={}",
                page, size, title, brand, year);
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByTitleContainingAndBrandContainingAndYear(
                title, brand, year, pageable
        );
        log.info("Got {} books from database", books.getTotalElements());
        redisCacheService.saveToCacheAsync(cacheKey, books);
        return books;
    }

    @Transactional
    public Book createBook(Book book) {
        log.debug("Creating book with vendorCode: {}", book.getVendorCode());
        if (bookRepository.findByVendorCode(book.getVendorCode()).isPresent()) {
            log.error("Book with vendorCode {} already exists", book.getVendorCode());
            throw new RuntimeException("Book with vendor code " + book.getVendorCode() + " already exists");
        }
        Book savedBook = bookRepository.save(book);
        log.info("Book created with ID: {}", savedBook.getId());
        clearCache();
        return savedBook;
    }

    @Transactional
    public Book updateBook(Long id, Book bookDetails) {
        log.debug("Updating book with ID: {}", id);
        Book book = findBookById(id);
        book.setVendorCode(bookDetails.getVendorCode());
        book.setTitle(bookDetails.getTitle());
        book.setYear(bookDetails.getYear());
        book.setBrand(bookDetails.getBrand());
        book.setStock(bookDetails.getStock());
        book.setPrice(bookDetails.getPrice());
        Book updatedBook = bookRepository.save(book);
        log.info("Book updated with ID: {}", updatedBook.getId());
        clearCache();
        return updatedBook;
    }

    @Transactional
    public void deleteBook(Long id) {
        log.debug("Deleting book with ID: {}", id);
        if (!bookRepository.existsById(id)) {
            log.error("Book with ID {} not found for deletion", id);
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Book deleted with ID: {}", id);
        clearCache();
    }

    private String generateCacheKey(int page, int size, String title, String brand, Integer year) {
        return "books:" + page + ":" + size + ":" + (title != null ? title : "") + ":"
                + (brand != null ? brand : "") + ":" + (year != null ? year : "0");
    }

    private void clearCache() {
        log.debug("Clearing cache with pattern: books:*");
        redisCacheService.clearCacheAsync("books:*");
    }

    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book with ID {} not found", id);
                    return new BookWasNotFoundException("Book not found with id: " + id);
                });
    }
}