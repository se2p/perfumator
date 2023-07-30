package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.EqualsOverrideDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EqualsOverrideDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("equals_override");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void setupPerfume() {
        perfume = new Perfume();
        perfume.setName("Override equals of superclass");
    }

    @BeforeEach
    void setupDetector() {
        detector = new EqualsOverrideDetector();
        detector.setConcreteDetectable(perfume);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NonTransitive.java", "Transitive.java"})
    void detect(@NotNull String testFileName) {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR.resolve("dependency"));
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve(testFileName));

        detector.setAnalysisContext(analysisContext);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);

        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("Perfumed");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(11, 9, 14, 9));
    }

    @Test
    void missingDependencies() {
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("DependenciesMissing.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
