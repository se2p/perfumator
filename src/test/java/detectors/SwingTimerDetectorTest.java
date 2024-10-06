package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.SwingTimerDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwingTimerDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR =
            DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("swing");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Swing Timer");

        detector = new SwingTimerDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        ast = parseAstForFile(TEST_FILES_DIR.resolve("SwingTimer.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(4);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SwingTimer");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(9, 15, 9, 54));

        detection = detections.get(1);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SwingTimer");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(13, 18, 18, 10));

        detection = detections.get(2);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SwingTimer");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(19, 22, 19, 60));

        detection = detections.get(3);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SwingTimer");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(20, 36, 20, 71));
    }
}
