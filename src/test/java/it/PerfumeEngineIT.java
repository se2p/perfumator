package it;

import com.fasterxml.jackson.core.type.TypeReference;
import de.jsilbereisen.perfumator.engine.DetectionEngine;
import de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.Test;
import test.AbstractJsonOutputTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerfumeEngineIT extends AbstractJsonOutputTest {

    private static final Path IT_TEST_PROJECT = Path.of("src", "additional-integration-test-resources",
            "it-project");

    @Test
    void analyseSourceDirectoryWithoutSerialization() {
        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder().build();

        List<DetectedInstance<Perfume>> detections = engine.detect(IT_TEST_PROJECT.resolve("src")).getDetections();
        detections.sort(null);

        assertThat(detections).hasSize(7);

        DetectedInstance<Perfume> perf = detections.get(0);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(6, 5, 9, 5));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("AtLeastXVarargsDetector");

        perf = detections.get(1);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(13, 9, 22, 9));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("DefensiveDefaultCaseDetector");

        perf = detections.get(2);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(9, 5, 11, 5));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("AtLeastXVarargsDetector");

        perf = detections.get(3);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(4, 1, 12, 1));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("NoUtilityInstantiationDetector");

        // Here, the order of the perfumes is different from the other tests, because the RelatedPattern is also
        // considered! (Is NULL in the JSON-listings)
        perf = detections.get(4);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes4");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(9, 22, 9, 48));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("PatternMatchingDetector");

        perf = detections.get(5);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes4");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(6, 5, 14, 5));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("EqualsBlueprintDetector");

        perf = detections.get(6);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes3");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(6, 5, 8, 5));
        assertThat(perf.getDetectable().getDetectorClassSimpleName()).isEqualTo("DefensiveNullCheckDetector");
    }

    @Test
    void analyseSourceDirectoryWithOutput() {
        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder().build();

        engine.detectAndSerialize(IT_TEST_PROJECT.resolve("src"),
                OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR), OutputFormat.JSON);

        // Check listing of detections
        List<DetectedInstance<Perfume>> detections = readList(new TypeReference<>() {},
                OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR.resolve("detections.json"));
        detections.sort(null);

        assertThat(detections).hasSize(7);

        DetectedInstance<Perfume> perf = detections.get(0);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(6, 5, 9, 5));

        perf = detections.get(1);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes1");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(13, 9, 22, 9));

        perf = detections.get(2);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(9, 5, 11, 5));

        perf = detections.get(3);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes2");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(4, 1, 12, 1));

        perf = detections.get(4);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes4");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(6, 5, 14, 5));

        perf = detections.get(5);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes4");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(9, 22, 9, 48));

        perf = detections.get(6);
        assertThat(perf.getTypeName()).isEqualTo("HasPerfumes3");
        assertThat(perf.getCodeRanges()).containsExactly(CodeRange.of(6, 5, 8, 5));

        // Check summary
        StatisticsSummary<Perfume> summary = readStatistics(new TypeReference<>() {},
                OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR.resolve("summary.json"), engine.getRegistry(),
                Perfume.class);

        assertThat(summary.getTotalAnalysedFiles()).isEqualTo(5);
        assertThat(summary.getAnalyzedFiles()).hasSize(5);
        assertThat(summary.getTotalDetections()).isEqualTo(7);
    }
}
