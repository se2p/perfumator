package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.MethodCallRteTestingDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MethodCallRteTestingDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("exception_testing");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Single Method call when testing for runtime exceptions");

        detector = new MethodCallRteTestingDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfumeFrameworkMethods() {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve("SingleMethodCallPerfume.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(3);
        detections.sort(DetectedInstance::compareTo);

        // JUnit method
        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SingleMethodCallPerfume");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(14, 9, 14, 79));

        // AssertJ method
        detection = detections.get(1);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SingleMethodCallPerfume");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(20, 9, 20, 49));

        // Other JUnit method
        detection = detections.get(2);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SingleMethodCallPerfume");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(26, 9, 26, 90));
    }

    @Test
    void detectPerfumeTryCatch() {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve("SingleMethodCallTryCatch.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SingleMethodCallTryCatch");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(12, 9, 17, 9));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SingleMethodCallMissingImports.java", "SingleMethodCallNotPerfumed.java",
            "SingleMethodCallTryCatchNotPerfumed.java"})
    void notPerfumed(@NotNull String testFileName) {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve(testFileName));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
