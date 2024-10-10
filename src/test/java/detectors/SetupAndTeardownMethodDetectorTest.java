package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.SetupAndTeardownMethodDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SetupAndTeardownMethodDetectorTest extends AbstractDetectorTest {
    private static final Path TEST_FILES_DIR = 
            DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("setup_and_teardown_methods");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Setup or teardown method");

        detector = new SetupAndTeardownMethodDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        ast = parseAstForFile(TEST_FILES_DIR.resolve("SetupAndTeardownMethods.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(4);
        
        DetectedInstance<Perfume> beforeAllDetection = detections.get(0);
        assertThat(beforeAllDetection.getDetectable()).isEqualTo(perfume);
        assertThat(beforeAllDetection.getTypeName()).isEqualTo("SetupAndTeardownMethods");
        assertThat(beforeAllDetection.getCodeRanges()).containsExactly(CodeRange.of(12, 5, 15, 5));

        DetectedInstance<Perfume> beforeEachDetection = detections.get(1);
        assertThat(beforeEachDetection.getDetectable()).isEqualTo(perfume);
        assertThat(beforeEachDetection.getTypeName()).isEqualTo("SetupAndTeardownMethods");
        assertThat(beforeEachDetection.getCodeRanges()).containsExactly(CodeRange.of(17, 5, 20, 5));

        DetectedInstance<Perfume> afterEachDetection = detections.get(2);
        assertThat(afterEachDetection.getDetectable()).isEqualTo(perfume);
        assertThat(afterEachDetection.getTypeName()).isEqualTo("SetupAndTeardownMethods");
        assertThat(afterEachDetection.getCodeRanges()).containsExactly(CodeRange.of(27, 5, 30, 5));

        DetectedInstance<Perfume> afterAllDetection = detections.get(3);
        assertThat(afterAllDetection.getDetectable()).isEqualTo(perfume);
        assertThat(afterAllDetection.getTypeName()).isEqualTo("SetupAndTeardownMethods");
        assertThat(afterAllDetection.getCodeRanges()).containsExactly(CodeRange.of(32, 5, 35, 5));
    }

    @Test
    void detectWithOwnAnnotations() {
        ast = parseAstForFile(TEST_FILES_DIR.resolve("SetupAndTeardownMethodsOwnAnnotations.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(4);

        DetectedInstance<Perfume> beforeAllDetection = detections.get(0);
        assertThat(beforeAllDetection.getDetectable()).isEqualTo(perfume);
        assertThat(beforeAllDetection.getTypeName()).isEqualTo("SetupAndTeardownMethodsOwnAnnotations");
        assertThat(beforeAllDetection.getCodeRanges()).containsExactly(CodeRange.of(7, 5, 10, 5));

        DetectedInstance<Perfume> beforeEachDetection = detections.get(1);
        assertThat(beforeEachDetection.getDetectable()).isEqualTo(perfume);
        assertThat(beforeEachDetection.getTypeName()).isEqualTo("SetupAndTeardownMethodsOwnAnnotations");
        assertThat(beforeEachDetection.getCodeRanges()).containsExactly(CodeRange.of(12, 5, 15, 5));

        DetectedInstance<Perfume> afterEachDetection = detections.get(2);
        assertThat(afterEachDetection.getDetectable()).isEqualTo(perfume);
        assertThat(afterEachDetection.getTypeName()).isEqualTo("SetupAndTeardownMethodsOwnAnnotations");
        assertThat(afterEachDetection.getCodeRanges()).containsExactly(CodeRange.of(22, 5, 25, 5));

        DetectedInstance<Perfume> afterAllDetection = detections.get(3);
        assertThat(afterAllDetection.getDetectable()).isEqualTo(perfume);
        assertThat(afterAllDetection.getTypeName()).isEqualTo("SetupAndTeardownMethodsOwnAnnotations");
        assertThat(afterAllDetection.getCodeRanges()).containsExactly(CodeRange.of(27, 5, 30, 5));
    } 
}
