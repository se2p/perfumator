package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.IteratorNextContractDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IteratorNextContractDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("iterator_next_contract");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void initPerfume() {
        perfume = new Perfume();
        perfume.setName("Iterator next() follows the contract");
    }

    @BeforeEach
    void init() {
        detector = new IteratorNextContractDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfumeRegularCase() {
        JavaParserFacade context = getAnalysisContext(parser);
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("IteratorNextContractPerfume.java"));

        detector.setAnalysisContext(context);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("IteratorNextContractPerfume");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(11);
        assertThat(detection.getEndingLineNumber()).isEqualTo(19);
    }

    @Test
    void detectPerfumeWithVariable() {
        JavaParserFacade context = getAnalysisContext(parser);
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("IteratorNextContractWithVariable.java"));

        detector.setAnalysisContext(context);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("IteratorNextContractWithVariable");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(13);
        assertThat(detection.getEndingLineNumber()).isEqualTo(21);
    }

    @Test
    void detectPerfumeVariants() {
        JavaParserFacade context = getAnalysisContext(parser);
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("IteratorNextContractPerfumeVariants.java"));

        detector.setAnalysisContext(context);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);
        detections.sort(DetectedInstance::compareTo);

        assertThat(detections).hasSize(4);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("MethodCallElseBranch");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(12);
        assertThat(detection.getEndingLineNumber()).isEqualTo(19);

        detection = detections.get(1);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("MethodCallLastStmt");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(26);
        assertThat(detection.getEndingLineNumber()).isEqualTo(33);

        detection = detections.get(2);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("VariableElseBranch");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(43);
        assertThat(detection.getEndingLineNumber()).isEqualTo(48);

        detection = detections.get(3);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("VariableLastStmt");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(58);
        assertThat(detection.getEndingLineNumber()).isEqualTo(65);
    }

    @Test
    void notPerfumedCases() {
        // File with multiple negative test cases
        JavaParserFacade context = getAnalysisContext(parser);
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("IteratorNextContractNotPerfumed.java"));

        detector.setAnalysisContext(context);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).isEmpty();
    }
}
