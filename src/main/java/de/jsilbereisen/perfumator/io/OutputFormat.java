package de.jsilbereisen.perfumator.io;

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
}
