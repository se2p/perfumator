package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.SingletonPatternDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SingletonPatternDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("singleton_pattern");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void setupPerfume() {
        perfume = new Perfume();
        perfume.setName("Singleton pattern");
    }

    @BeforeEach
    void setupDetector() {
        detector = new SingletonPatternDetector();
        detector.setConcreteDetectable(perfume);
        detector.setAnalysisContext(getAnalysisContext(parser));
    }

    @Test
    void detectVariants() {
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("SingletonPatternPerfume.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);
        detections.sort(DetectedInstance::compareTo);

        assertThat(detections).hasSize(4);

        checkDetectedInstance(detections.get(0), perfume, "EnumSingleton", CodeRange.of(27, 5, 35, 5));
        checkDetectedInstance(detections.get(1), perfume, "PublicFactoryMethod", CodeRange.of(12, 5, 23, 5));
        checkDetectedInstance(detections.get(2), perfume, "PublicInstanceField", CodeRange.of(7, 5, 10, 5));
        checkDetectedInstance(detections.get(3), perfume, "SerializableSingleton", CodeRange.of(38, 5, 51, 5));
    }

    @Test
    void notPerfumedVariants() {
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("NotPerfumedSingleton.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
