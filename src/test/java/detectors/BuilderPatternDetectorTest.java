package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.BuilderPatternDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuilderPatternDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("builder_pattern");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    @BeforeAll
    static void setup() {
        perfume = new Perfume();
        perfume.setName("Builder pattern");

        detector = new BuilderPatternDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfumed() {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve("BuilderPatternPerfume.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        checkDetectedInstance(detections.get(0), perfume, "BuilderPatternPerfume", CodeRange.of(7, 1, 56, 1));
    }

    @Test
    void detectPerfumedVariations() {

        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve("BuilderPatternPerfumeVariation.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(3);

        checkDetectedInstance(detections.get(0), perfume, "BuilderPatternPerfumeVariation", CodeRange.of(3, 1, 39, 1));
        checkDetectedInstance(detections.get(1), perfume, "SecondTopLevelClass", CodeRange.of(41, 1, 57, 1));
        checkDetectedInstance(detections.get(2), perfume, "ThirdClass", CodeRange.of(59, 1, 81, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"BuilderPatternInvalidBuilderClasses.java", "BuilderPatternInvalidTopLevelClass.java"})
    void invalidCases(@NotNull String testFileName) {
        CompilationUnit ast = parseAstForFile(TEST_FILES_DIR.resolve(testFileName));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
