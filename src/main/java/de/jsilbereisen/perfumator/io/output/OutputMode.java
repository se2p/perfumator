package de.jsilbereisen.perfumator.io.output;

/**
 * Constants for representing different output modes.
 */
public enum OutputMode {

    /**
     * Output mode "single". Means that all output-files are generated in a single directory.
     */
    SINGLE,

    /**
     * <p>
     * Output mode "directory". Means that the output-files should be generated in directories that are
     * suitable.
     * </p><p>
     * For example, the overall statistics should be generated in the output-root directory, while
     * listings of detections from a single source file should be generated in a directory that matches the
     * package-structure of the project.<br/>
     * Short: The output is structured in directories, matching the structure of the analysed project.
     * </p>
     */
    DIRECTORY
}
