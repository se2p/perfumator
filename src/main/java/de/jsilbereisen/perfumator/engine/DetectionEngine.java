package de.jsilbereisen.perfumator.engine;

import org.apache.commons.lang3.SerializationException;
import org.jetbrains.annotations.NotNull;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.util.JsonDeserializationUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for an engine that runs an analysis for detection certain detectable code structures.
 * The concrete code structures to be analysed and the classes that actually detect those are
 * given by a {@link DetectableRegistry}.
 *
 * @param <T> The concrete type of detectable code structure.
 */
public interface DetectionEngine<T extends Detectable> {

    /**
     * Runs an analysis on the given Java source file or all (recursively) found Java source files in the given
     * directory, to find {@link DetectedInstance}s of type {@link T}. Returns a list of all findings.
     *
     * @param sources The path to the Java source file to analyse, or to a directory that should be recursively
     *                scanned for Java source files which are then analysed.
     * @throws IllegalArgumentException If the given {@link Path} to analyse is neither a Java source file, no a
     *                                  directory.
     * @throws AnalysisException        If a problem occurs when running the analysis, for example while walking the file
     *                                  tree of the directory.
     */
    List<DetectedInstance<T>> detect(@NotNull Path sources);

    /**
     * <p>
     * Runs an analysis on the given Java source file or all (recursively) found Java source files in the given
     * directory, to find {@link DetectedInstance}s of type {@link T}. The findings are serialized, taking the given
     * {@link OutputFormat} and {@link OutputConfiguration} into account.
     * </p>
     * <p>
     * Logically, the analysis run by this method must be the same as by the {@link #detect} method,
     * but this method should be able to handle larger Projects/Directories to analyse by batching the output, in
     * order to avoid OOMs (Out Of Memory Errors) or similar problems.<br/>
     * As a consequence, this method has no return value. If you want to deserialize the results,
     * check out the {@link JsonDeserializationUtil} class.
     * </p>
     *
     * @param sources The path to the Java source file to analyse, or to a directory that should be recursively
     *                scanned for Java source files which are then analysed.
     * @param config  Configuration for the output.
     * @param format  Desired format for the output.
     * @throws UnsupportedOperationException If the given {@link OutputFormat} is not supported.
     * @throws SerializationException        If serialization of any analysis results fails, e.g. because
     *                                       an {@link IOException} occurred.
     * @throws IllegalArgumentException      If the given {@link Path} to analyse is neither a Java source file, no a
     *                                       directory, or if the output-directory in the given {@link OutputConfiguration}
     *                                       is non-existent or non-empty.
     * @throws AnalysisException             If a problem occurs when running the analysis, for example while walking the file
     *                                       tree of the directory.
     * @see JsonDeserializationUtil
     */
    void detectAndSerialize(@NotNull Path sources, @NotNull OutputConfiguration config, @NotNull OutputFormat format)
            throws SerializationException;

    /**
     * Returns the {@link DetectableRegistry} that is used for analysis by this engine.
     *
     * @return The registry.
     * @see DetectableRegistry
     */
    @NotNull DetectableRegistry<T> getRegistry();
}
