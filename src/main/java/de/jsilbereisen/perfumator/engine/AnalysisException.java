package de.jsilbereisen.perfumator.engine;

import org.jetbrains.annotations.NotNull;

public class AnalysisException extends RuntimeException {

    public AnalysisException(@NotNull String message) {
        super(message);
    }

    public AnalysisException(@NotNull Throwable cause) {
        super(cause);
    }

    public AnalysisException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
