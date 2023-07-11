package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.DefensiveNullCheckDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.DetectedInstanceComparator;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefensiveNullCheckDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new DefensiveNullCheckDetector();

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("defensive_null_check");

    private static Perfume perfume;

    @BeforeAll
    static void setup() {
        perfume = new Perfume();
        perfume.setName("Defensive null check");
        DETECTOR.setConcreteDetectable(perfume);
    }

    @Test
    void detectValidPerfumes() {
        Path validPerfumesFile = TEST_FILES_DIR.resolve("DefensiveNullCheckPerfume.java");
        CompilationUnit ast = parseAstForFile(validPerfumesFile);
        List<DetectedInstance<Perfume>> detectedInstances = DETECTOR.detect(ast);

        assertThat(detectedInstances).hasSize(6);

        detectedInstances.sort(new DetectedInstanceComparator<>());

        DetectedInstance<Perfume> first = detectedInstances.get(0);
        assertThat(first.getDetectable()).isEqualTo(perfume);
        assertThat(first.getTypeName()).isEqualTo("DefensiveNullCheckPerfume");
        assertThat(first.getBeginningLineNumber()).isEqualTo(12);
        assertThat(first.getEndingLineNumber()).isEqualTo(17);
    }

    @Test
    void doNotDetectNonPerfumed() {
        Path nonValidExamples = TEST_FILES_DIR.resolve("DefensiveNullCheckNotPerfumed.java");
        CompilationUnit ast = parseAstForFile(nonValidExamples);
        List<DetectedInstance<Perfume>> detectedInstances = DETECTOR.detect(ast);

        assertThat(detectedInstances).isEmpty();
    }
}
