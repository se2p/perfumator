package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.OptimizedEnumCollectionsDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizedEnumCollectionsDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILE = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("OptimizedEnumCollectionsPerfume"
            + ".java");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void setupPerfume() {
        perfume = new Perfume();
        perfume.setName("Use optimized collections for Enums");
    }

    @BeforeEach
    void setupDetector() {
        detector = new OptimizedEnumCollectionsDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        JavaParserFacade analysisContext = getAnalysisContext(parser);
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, TEST_FILE);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(7);

        String expectedTypeName = "Perfumed";
        // Check EnumSet factory method calls
        checkDetectedInstance(detections.get(0), perfume, expectedTypeName, CodeRange.of(15, 40, 15, 61));
        checkDetectedInstance(detections.get(1), perfume, expectedTypeName, CodeRange.of(16, 40, 16, 66));
        checkDetectedInstance(detections.get(2), perfume, expectedTypeName, CodeRange.of(17, 40, 17, 67));
        checkDetectedInstance(detections.get(3), perfume, expectedTypeName, CodeRange.of(18, 40, 18, 78));
        checkDetectedInstance(detections.get(4), perfume, expectedTypeName, CodeRange.of(19, 42, 19, 65));
        checkDetectedInstance(detections.get(5), perfume, expectedTypeName, CodeRange.of(20, 44, 20, 73));

        // Check EnumMap constructor usage
        checkDetectedInstance(detections.get(6), perfume, expectedTypeName, CodeRange.of(23, 43, 23, 69));
    }
}
