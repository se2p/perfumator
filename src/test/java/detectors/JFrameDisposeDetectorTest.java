package detectors;

import com.github.javaparser.ast.CompilationUnit;
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

import static org.assertj.core.api.Assertions.assertThat;

public class JFrameDisposeDetectorTest extends AbstractDetectorTest {
    
    private static final Path TEST_FILES_DIR = 
            DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("swing");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Assert All");

        detector = new JFrameDisposeDetector();
        detector.setConcreteDetectable(perfume);
        
        ast = parseAstForFile(TEST_FILES_DIR.resolve("JFrameDispose.java"));
    }

    @Test
    void detect() {
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(3);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("JFrameDispose");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(18, 9, 18, 24));

        detection = detections.get(1);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("JFrameDispose");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(19, 9, 19, 24));

        detection = detections.get(2);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("JFrameDispose");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(20, 9, 20, 24));
    }
}
