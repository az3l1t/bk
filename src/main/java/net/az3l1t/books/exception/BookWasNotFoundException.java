package net.az3l1t.books.exception;

public class BookWasNotFoundException extends RuntimeException {
    public BookWasNotFoundException(String message) {
        super(message);
    }
}
