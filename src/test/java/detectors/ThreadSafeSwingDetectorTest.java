package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.ThreadSafeSwingDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadSafeSwingDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILE = 
            DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("swing").resolve("InvokeLaterInvokeAndWait.java");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Thread safe Swing");

        detector = new ThreadSafeSwingDetector();
        detector.setConcreteDetectable(perfume);

        ast = parseAstForFile(TEST_FILE);
    }

    @Test
    void detect() {
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(2);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("InvokeLaterInvokeAndWait");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(9, 9, 11, 10));

        detection = detections.get(1);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("InvokeLaterInvokeAndWait");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(13, 9, 15, 10));
    }
}
