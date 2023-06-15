package de.jsilbereisen.perfumator.model;

/**
 * Exception for failures when {@link Perfume}s are loaded from their JSON representation.
 */
public class PerfumeLoadException extends RuntimeException {

    public PerfumeLoadException() {
        super();
    }

    public PerfumeLoadException(String message) {
        super(message);
    }

    public PerfumeLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
