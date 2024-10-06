package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.ParameterizedTestDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterizedTestDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("parameterized_tests");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Parameterized Test");

        detector = new ParameterizedTestDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        ast = parseAstForFile(TEST_FILES_DIR.resolve("ParameterizedTests.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);

        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("ParameterizedTests");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(22, 5, 26, 5));
    }

    @Test
    void detectNoPerfumeForOnwAnnotation() {
        ast = parseAstForFile(TEST_FILES_DIR.resolve("ParameterizedTestsOwnAnnotation.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);
        
        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("ParameterizedTestsOwnAnnotation");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(17, 5, 21, 5));
    }
}
