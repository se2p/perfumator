package de.jsilbereisen.perfumator.io;

/**
 * Available output formats for the command line application.
 */
public enum OutputFormat {

    JSON("JSON", "JavaScript Object Notation"),

    CSV("csv", "Comma-separated values");

    private final String abbreviation;

    private final String longName;

    OutputFormat(String abbreviation, String longName) {
        this.abbreviation = abbreviation;
        this.longName = longName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getLongName() {
        return longName;
    }

    /**
     * Returns the {@link OutputFormat} that is used as a default for the application.
     */
    public static OutputFormat getDefault() {
        return OutputFormat.JSON;
    }
}
