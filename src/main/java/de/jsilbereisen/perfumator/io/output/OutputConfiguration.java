package de.jsilbereisen.perfumator.io.output;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds configuration information for an {@link AbstractOutputGenerator} such as the target directory where the output
 * shall be produced, the batch size (max amount of serialized {@link de.jsilbereisen.perfumator.model.DetectedInstance}s
 * per output-file for listings), the {@link OutputMode} and whether to only produce statistics.
 */
@Getter
@Setter
@Accessors(chain = true)
public class OutputConfiguration {

    /**
     * Maximal amount of output instances in a single listing file.
     */
    public static final int MAX_BATCH_SIZE = 500_000;

    /**
     * Minimal amount of output instances in a single listing file.
     */
    public static final int MIN_BATCH_SIZE = 100;

    /**
     * Default amount of output instances in a single listing file.
     */
    public static final int DEFAULT_BATCH_SIZE = 10000;

    private final Path outputDirectory;

    @Range(from = MIN_BATCH_SIZE, to = MAX_BATCH_SIZE)
    private int batchSize = DEFAULT_BATCH_SIZE;

    private OutputMode outputMode = OutputMode.SINGLE;

    private boolean statisticsOnly = false;

    private OutputConfiguration(@NotNull Path outputDirectory) {
        if (!Files.isDirectory(outputDirectory)) {
            throw new IllegalArgumentException("Path does not represent an existing directory.");
        }

        this.outputDirectory = outputDirectory;
    }

    @NotNull
    public static OutputConfiguration from(@NotNull Path targetDirectory) {
        return new OutputConfiguration(targetDirectory);
    }

    /**
     * Switching of output mode not supported yet. TODO
     *
     * @throws UnsupportedOperationException always.
     */
    public void setOutputMode(@NotNull OutputMode outputMode) {
        throw new UnsupportedOperationException("Switching of output mode not supported yet. Current mode: " + outputMode);
    }

    /**
     * Setter for the batch size of detections that are put into a single listing.
     *
     * @param batchSize The new batch size.
     * @return {@code this}.
     * @throws IllegalArgumentException If the given batch size is not between (inclusive) {@link #MIN_BATCH_SIZE}
     *                                  and {@link #MAX_BATCH_SIZE}.
     */
    public @NotNull OutputConfiguration setBatchSize(int batchSize) {
        if (batchSize < MIN_BATCH_SIZE || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch size out of bounds. Must be between (inclusive) "
                    + MIN_BATCH_SIZE + " and " + MAX_BATCH_SIZE);
        }
        this.batchSize = batchSize;
        return this;
    }

}
