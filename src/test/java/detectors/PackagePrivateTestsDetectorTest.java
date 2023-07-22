package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.PackagePrivateTestsDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackagePrivateTestsDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("package_private_tests");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("JUnit 5 tests can be package-private");

        detector = new PackagePrivateTestsDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfume() {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve("PackagePrivateTestsPerfume.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("PackagePrivateTestsPerfume");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(6, 1, 18, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PackagePrivateTestsClassPublic",
            "PackagePrivateTestsNoImports", "PackagePrivateTestsMethodPublic"})
    void notPerfumed(String testFileName) {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve(testFileName + ".java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
