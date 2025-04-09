package net.az3l1t.books;

import net.az3l1t.books.model.Book;
import net.az3l1t.books.repository.BookRepository;
import net.az3l1t.books.service.BookService;
import net.az3l1t.books.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .vendorCode("BOOK001")
                .title("Test Book")
                .year(2023)
                .brand("TestBrand")
                .stock(10)
                .price(29.99)
                .build();
    }

    @Test
    void getBooks_ReturnsCachedBooks_WhenCacheExists() {
        Page<Book> cachedPage = new PageImpl<>(Collections.singletonList(testBook));
        when(redisCacheService.getFromCache("books:0:10:::0")).thenReturn(cachedPage);

        Page<Book> result = bookService.getBooks(0, 10, null, null, null);

        assertEquals(cachedPage, result);
        verify(redisCacheService, times(1)).getFromCache(anyString());
        verify(bookRepository, never()).findByTitleContainingAndBrandContainingAndYear(any(), any(), any(), any());
    }

    @Test
    void getBooks_ReturnsBooksFromRepo_WhenCacheIsEmpty() {
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(testBook));
        when(redisCacheService.getFromCache("books:0:10:::0")).thenReturn(null);
        when(bookRepository.findByTitleContainingAndBrandContainingAndYear(null, null, null, PageRequest.of(0, 10)))
                .thenReturn(bookPage);

        Page<Book> result = bookService.getBooks(0, 10, null, null, null);

        assertEquals(bookPage, result);
        verify(redisCacheService, times(1)).getFromCache(anyString());
        verify(bookRepository, times(1)).findByTitleContainingAndBrandContainingAndYear(any(), any(), any(), any());
        verify(redisCacheService, times(1)).saveToCacheAsync(anyString(), eq(bookPage));
    }

    @Test
    void createBook_SavesNewBook_WhenVendorCodeIsUnique() {
        when(bookRepository.findByVendorCode("BOOK001")).thenReturn(Optional.empty());
        when(bookRepository.save(testBook)).thenReturn(testBook);

        Book result = bookService.createBook(testBook);

        assertEquals(testBook, result);
        verify(bookRepository, times(1)).findByVendorCode("BOOK001");
        verify(bookRepository, times(1)).save(testBook);
        verify(redisCacheService, times(1)).clearCacheAsync("books:*");
    }

    @Test
    void updateBook_UpdatesExistingBook_WhenIdExists() {
        Book updatedDetails = Book.builder()
                .vendorCode("BOOK002")
                .title("Updated Book")
                .year(2024)
                .brand("NewBrand")
                .stock(5)
                .price(39.99)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedDetails);

        Book result = bookService.updateBook(1L, updatedDetails);

        assertEquals("BOOK002", result.getVendorCode());
        assertEquals("Updated Book", result.getTitle());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(redisCacheService, times(1)).clearCacheAsync("books:*");
    }

    @Test
    void deleteBook_DeletesBook_WhenIdExists() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
        verify(redisCacheService, times(1)).clearCacheAsync("books:*");
    }

    @Test
    void createBook_ThrowsException_WhenVendorCodeExists() {
        when(bookRepository.findByVendorCode("BOOK001")).thenReturn(Optional.of(testBook));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookService.createBook(testBook));
        assertEquals("Book with vendor code BOOK001 already exists", exception.getMessage());
        verify(bookRepository, times(1)).findByVendorCode("BOOK001");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_ThrowsException_WhenIdNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookService.updateBook(1L, testBook));
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_ThrowsException_WhenIdNotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookService.deleteBook(1L));
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, never()).deleteById(anyLong());
    }
}
