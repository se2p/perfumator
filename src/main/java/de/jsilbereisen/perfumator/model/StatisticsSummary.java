package de.jsilbereisen.perfumator.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An overview over statistics for an analysis run.
 * Has public accessors and a default constructor in order to work with the
 * <i>Jackson Object Mapper</i> for (de-)serialization, but it is recommended to
 * only construct new instances via the static {@link #from} factory method and to only
 * add detections to the statistics via the {@link #addToStatistics} method, in order to
 * keep the data consistent.
 *
 * @param <T> The type of {@link Detectable} that was analysed.
 */
@Data
@Accessors(chain = true)
public class StatisticsSummary<T extends Detectable> {

    private int totalDetections;

    private int totalAnalysedFiles;

    private Map<T, Statistics<T>> detectableStatistics = new HashMap<>();

    private Set<Path> analyzedFiles = new LinkedHashSet<>();

    /**
     * Returns a new {@link StatisticsSummary}, initialized with empty {@link Statistics} for all
     * registered {@link T} in the given {@link DetectableRegistry}.
     */
    @NotNull
    public static <T extends Detectable> StatisticsSummary<T> from(@NotNull DetectableRegistry<T> registry) {
        StatisticsSummary<T> summary = new StatisticsSummary<>();

        for (T detectable : registry.getRegisteredDetectables()) {
            summary.detectableStatistics.put(detectable, new Statistics<T>().setDetectable(detectable));
        }

        return summary;
    }

    public void addToStatistics(@NotNull Collection<DetectedInstance<T>> detections) {
        for (DetectedInstance<T> det : detections) {
            addToStatistics(det);
        }
    }

    /**
     * Adds a detection to the captured statistics.
     *
     * @param detection The detection.
     */
    public void addToStatistics(@NotNull DetectedInstance<T> detection) {
        T detectable = detection.getDetectable();

        if (detectable == null) {
            throw new IllegalArgumentException("Detection must have a linked Detectable to be added to statistics!");
        }

        Statistics<T> stats = detectableStatistics.get(detectable);

        if (stats == null) {
            stats = new Statistics<T>().setDetectable(detectable);
            detectableStatistics.put(detectable, stats);
        }

        addDetection(detection.getSourceFile());
        stats.addDetection(detection.getSourceFile());
    }

    /**
     * Adds the path to the statistics of total analysed files without any detection.
     *
     * @param path The path to add.
     */
    public void addToStatistics(@NotNull Path path) {
        analyzedFiles.add(path);
        totalAnalysedFiles = analyzedFiles.size();
    }

    private void addDetection(@Nullable Path path) {
        ++totalDetections;

        if (path != null) {
            analyzedFiles.add(path);
        }

        totalAnalysedFiles = analyzedFiles.size();
    }

    @Data
    @Accessors(chain = true)
    public static class Statistics<T extends Detectable> {

        @JsonUnwrapped(prefix = "detectable_")
        private T detectable;

        private int totalDetections = 0;

        private int uniqueFilesWithDetection;

        private Set<Path> filesWithDetection = new LinkedHashSet<>();

        /**
         * Increases the amount of occurrences by 1 and adds the given Path to
         * the set of files with at least one detection.
         *
         * @param path The path to the file where the detection stems from.
         */
        public void addDetection(@Nullable Path path) {
            ++totalDetections;

            if (path != null) {
                filesWithDetection.add(path);
            }

            uniqueFilesWithDetection = filesWithDetection.size();
        }
    }
}
