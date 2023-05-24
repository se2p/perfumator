package de.jsilbereisen.perfumator.i18n;

/**
 * Exception for failures when {@link Internationalizable#internationalize} is called.
 */
public class InternationalizationException extends RuntimeException {

    public InternationalizationException() {
        super();
    }

    public InternationalizationException(String message) {
        super(message);
    }

    public InternationalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
