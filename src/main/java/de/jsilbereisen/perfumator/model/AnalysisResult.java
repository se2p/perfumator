package de.jsilbereisen.perfumator.model;

import lombok.Value;

import java.util.List;

/**
 * Result of an analysis run.
 *
 * @param <T> The type of {@link Detectable} that was analysed.
 */
@Value
public class AnalysisResult<T extends Detectable> {

    List<DetectedInstance<T>> detections;

    StatisticsSummary<T> summary;
}
