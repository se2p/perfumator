package de.jsilbereisen.perfumator.model;

import lombok.Value;

import java.util.List;
import java.util.UUID;

/**
 * Result of an analysis run.
 *
 * @param <T> The type of {@link Detectable} that was analysed.
 */
@Value
public class AnalysisResult<T extends Detectable> {

    UUID analysisResultId = UUID.randomUUID();

    List<DetectedInstance<T>> detections;

    StatisticsSummary<T> summary;
}
