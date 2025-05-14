package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.JFrameDisposeDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JFrameDisposeDetectorTest extends AbstractDetectorTest {
    
    private static final Path TEST_FILES_DIR = 
            DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("swing");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("JFrameDispose");

        detector = new JFrameDisposeDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        JavaParserFacade analysisContext = getAnalysisContext(parser, TEST_FILES_DIR);
        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("JFrameDispose.java"));
        detector.setAnalysisContext(analysisContext);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        final int NUM_DETECTIONS = 5;
        assertThat(detections).hasSize(NUM_DETECTIONS);
        Map<Integer, CodeRange> codeRanges = Map.of(
                0, CodeRange.of(15, 13, 15, 27),
                1, CodeRange.of(25, 9, 25, 24),
                2, CodeRange.of(26, 9, 26, 24),
                3, CodeRange.of(27, 9, 27, 24),
                4, CodeRange.of(28, 9, 28, 38)
        );
        assertThat(detections).hasSize(5);
        for (int i = 0; i < NUM_DETECTIONS; i++) {
            DetectedInstance<Perfume> detected = detections.get(i);
            assertThat(detected.getDetectable()).isEqualTo(perfume);
            assertThat(detected.getTypeName()).isEqualTo("JFrameDispose");
            assertThat(detected.getCodeRanges()).containsExactly(codeRanges.get(i));
        }
    }
}
