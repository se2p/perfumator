package it;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import test.AbstractJsonOutputTest;

import de.jsilbereisen.perfumator.engine.DetectionEngine;
import de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerfumeEngineIT extends AbstractJsonOutputTest {

    private static final Path IT_TEST_PROJECT = Path.of("src", "additional-integration-test-resources",
            "it-project");

    @Test
    void analyseSourceDirectoryWithoutSerialization() {
        DetectionEngine<Perfume> engine = new PerfumeDetectionEngine();

        List<DetectedInstance<Perfume>> detections = engine.detect(IT_TEST_PROJECT.resolve("src"));
        detections.sort(DetectedInstance::compareTo);

        assertThat(detections).hasSize(6);

        DetectedInstance<Perfume> perf1 = detections.get(0);
        assertThat(perf1.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf1.getBeginningLineNumber()).isEqualTo(6);
        assertThat(perf1.getDetectable().getDetectorClassSimpleName()).isEqualTo("AtLeastXVarargsDetector");

        DetectedInstance<Perfume> perf2 = detections.get(1);
        assertThat(perf2.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf2.getBeginningLineNumber()).isEqualTo(13);
        assertThat(perf2.getDetectable().getDetectorClassSimpleName()).isEqualTo("DefensiveDefaultCaseDetector");

        DetectedInstance<Perfume> perf3 = detections.get(2);
        assertThat(perf3.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf3.getBeginningLineNumber()).isEqualTo(9);
        assertThat(perf3.getDetectable().getDetectorClassSimpleName()).isEqualTo("AtLeastXVarargsDetector");

        DetectedInstance<Perfume> perf4 = detections.get(3);
        assertThat(perf4.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf4.getBeginningLineNumber()).isEqualTo(4);
        assertThat(perf4.getDetectable().getDetectorClassSimpleName()).isEqualTo("NoUtilityInitializationDetector");

        DetectedInstance<Perfume> perf5 = detections.get(4);
        assertThat(perf5.getTypeName()).isEqualTo("HasPerfumes4");
        assertThat(perf5.getBeginningLineNumber()).isEqualTo(6);
        assertThat(perf5.getDetectable().getDetectorClassSimpleName()).isEqualTo("EqualsBlueprintDetector");

        DetectedInstance<Perfume> perf6 = detections.get(5);
        assertThat(perf6.getTypeName()).isEqualTo("HasPerfumes3");
        assertThat(perf6.getBeginningLineNumber()).isEqualTo(6);
        assertThat(perf6.getDetectable().getDetectorClassSimpleName()).isEqualTo("DefensiveNullCheckDetector");
    }

    @Test
    void analyseSourceDirectoryWithOutput() {
        DetectionEngine<Perfume> engine = new PerfumeDetectionEngine();

        engine.detectAndSerialize(IT_TEST_PROJECT.resolve("src"),
                OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR), OutputFormat.JSON);

        // Check listing of detections
        List<DetectedInstance<Perfume>> detections = readList(new TypeReference<>() {
                                                              },
                OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR.resolve("detections.json"));
        detections.sort(DetectedInstance::compareTo);

        assertThat(detections).hasSize(6);

        DetectedInstance<Perfume> perf1 = detections.get(0);
        assertThat(perf1.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf1.getBeginningLineNumber()).isEqualTo(6);

        DetectedInstance<Perfume> perf2 = detections.get(1);
        assertThat(perf2.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf2.getBeginningLineNumber()).isEqualTo(13);

        DetectedInstance<Perfume> perf3 = detections.get(2);
        assertThat(perf3.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf3.getBeginningLineNumber()).isEqualTo(9);

        DetectedInstance<Perfume> perf4 = detections.get(3);
        assertThat(perf4.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf4.getBeginningLineNumber()).isEqualTo(4);

        DetectedInstance<Perfume> perf5 = detections.get(4);
        assertThat(perf5.getTypeName()).isEqualTo("HasPerfumes4");
        assertThat(perf5.getBeginningLineNumber()).isEqualTo(6);

        DetectedInstance<Perfume> perf6 = detections.get(5);
        assertThat(perf6.getTypeName()).isEqualTo("HasPerfumes3");
        assertThat(perf6.getBeginningLineNumber()).isEqualTo(6);

        // Check summary
        StatisticsSummary<Perfume> summary = readStatistics(new TypeReference<>() {
                                                            },
                OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR.resolve("summary.json"), engine.getRegistry(),
                Perfume.class);

        assertThat(summary.getTotalAnalysedFiles()).isEqualTo(5);
        assertThat(summary.getAnalyzedFiles()).hasSize(5);
        assertThat(summary.getTotalDetections()).isEqualTo(6);
    }

}
