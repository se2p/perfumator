package de.jsilbereisen.perfumator.io.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Available output formats for the command line application.
 */
@Getter
@RequiredArgsConstructor
public enum OutputFormat {

    JSON("JSON", "JavaScript Object Notation", ".json"),

    CSV("csv", "Comma-separated values", ".csv");

    private final String abbreviation;

    private final String longName;

    private final String fileExtension;

    /**
     * Returns the {@link OutputFormat} that is used as a default for the application.
     */
    public static OutputFormat getDefault() {
        return OutputFormat.JSON;
    }
}
