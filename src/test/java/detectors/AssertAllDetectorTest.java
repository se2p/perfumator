package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.AssertAllDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssertAllDetectorTest extends AbstractDetectorTest {
    
    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("assert_all");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Assert All");

        detector = new AssertAllDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detectStaticImport() {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR);
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("AssertAllStaticImport.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);

        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("AssertAllStaticImport");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(15, 9, 20, 9));
    }

    @Test
    void detectWithoutStaticImport() {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR);
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("AssertAllNoStaticImport.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);

        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("AssertAllNoStaticImport");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(14, 9, 19, 9));
    }

    @Test
    void detectWithWildcardImport() {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR);
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("AssertionsStaticWildcardImport.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);

        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("AssertionsStaticWildcardImport");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(15, 9, 20, 9));
    }

    @Test
    void detectNoPerfumeForDifferentAssertAllMethod() {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR);
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("AssertAllNoPerfume.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
