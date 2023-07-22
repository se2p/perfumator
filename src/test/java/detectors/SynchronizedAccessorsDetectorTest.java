package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.SynchronizedAccessorsDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.DetectedInstanceComparator;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SynchronizedAccessorsDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILE = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("SynchronizedAccessorsPerfume.java");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void setup() {
        perfume = new Perfume();
        perfume.setName("Synchronized Accessors");

        detector = new SynchronizedAccessorsDetector();
        detector.setConcreteDetectable(perfume);

        ast = parseAstForFile(TEST_FILE);
    }

    @Test
    void detect() {
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);
        detections.sort(new DetectedInstanceComparator<>());

        assertThat(detections).hasSize(3);

        DetectedInstance<Perfume> detected = detections.get(0);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("SynchronizedAccessorsPerfume");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(12, 5, 14, 5),
                CodeRange.of(16, 5, 18, 5));

        detected = detections.get(1);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("SynchronizedAccessorsPerfume");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(21, 5, 25, 5),
                CodeRange.of(27, 5, 31, 5));

        detected = detections.get(2);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("WithOverloads");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(36, 9, 38, 9),
                CodeRange.of(40, 9, 42, 9), CodeRange.of(44, 9, 46, 9));
    }
}
