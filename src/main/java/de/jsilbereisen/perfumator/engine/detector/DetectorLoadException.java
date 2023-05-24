package de.jsilbereisen.perfumator.engine.detector;

/**
 * Exception that occurs when loading/instancing {@link Detector} for a
 * {@link de.jsilbereisen.perfumator.model.Detectable}. Is also thrown if no {@link Detector}
 * is found for a {@link de.jsilbereisen.perfumator.model.Perfume}.
 */
public class DetectorLoadException extends RuntimeException {

    public DetectorLoadException() {
        super();
    }

    public DetectorLoadException(String message) {
        super(message);
    }

    public DetectorLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
