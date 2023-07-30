package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.CompareToAndEqualsPairDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompareToAndEqualsPairDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("compareTo_and_equals");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void setupPerfume() {
        perfume = new Perfume();
        perfume.setName("Override \"compareTo\" with \"equals\"");
    }

    @BeforeEach
    void setupDetector() {
        detector = new CompareToAndEqualsPairDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR.resolve("dependency"));
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("CompareToAndEqualsOverridePerfume.java"));

        detector.setAnalysisContext(analysisContext);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(2);
        detections.sort(null);

        DetectedInstance<Perfume> detected = detections.get(0);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("Perfumed");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(8, 9, 11, 9), CodeRange.of(13, 9, 16, 9));

        detected = detections.get(1);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("PerfumedTransitive");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(20, 9, 23, 9), CodeRange.of(25, 9, 28, 9));
    }
}
