package de.jsilbereisen.perfumator.io.output;

import org.jetbrains.annotations.NotNull;

import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;

import java.io.IOException;
import java.util.List;

/**
 * Interface for an output generator of any format. Also offers constants for consistent file pre- and suffixing.
 *
 * @param <T> The {@link Detectable}, for which the output of its {@link DetectedInstance}s is generated for.
 */
public interface OutputGenerator<T extends Detectable> extends HasOutputFormat, HasOutputConfiguration {

    /**
     * Connects the file name and the suffix(es) applied to it.
     */
    String SUFFIX_CONNECTOR = "_";

    /**
     * Suffix for statistical summary files. For example, an overview for the
     * unit "foo" with output format JSON should be named "foo_summary.json"
     */
    String SUMMARY_FILE_SUFFIX = "summary";

    /**
     * Suffix for files that list all detections. For example, a listing of detections for the
     * unit "foo" with output format JSON should be named "foo_detections.json"
     */
    String DETECTIONS_FILE_SUFFIX = "detections";

    /**
     * Regex-Pattern for file names (without file extension) with listings of detections WITHOUT
     * the file type.
     */
    String DETECTIONS_FILES_NAME_PATTERN = "^([a-zA-Z0-9_-]+" + SUFFIX_CONNECTOR + ")?"
            + DETECTIONS_FILE_SUFFIX + "(" + SUFFIX_CONNECTOR + "\\d+)?";

    /**
     * Handles the given list of {@link DetectedInstance}s of {@link T}.
     *
     * @param detectedInstances The list of detections to handle.
     */
    void handle(@NotNull List<DetectedInstance<T>> detectedInstances) throws IOException;

    /**
     * Completes the output by generating all missing statistical summaries or overviews.<br/>
     * If there is no change of state between multiple calls to this method, the operation
     * must be idempotent.
     */
    void complete() throws IOException;
}
