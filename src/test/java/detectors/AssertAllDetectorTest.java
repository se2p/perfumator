package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
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
    
    private static final Path TEST_FILE = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("AssertAllPerfume.java");

    private static Perfume perfume;

    private static Detector<Perfume> detector;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Assert All");

        detector = new de.jsilbereisen.perfumator.engine.detector.perfume.AssertAllDetector();
        detector.setConcreteDetectable(perfume);

        ast = parseAstForFile(TEST_FILE);
    }

    @Test
    void detect() {
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);

        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("AssertAllPerfume");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(16, 9, 21, 9));
    }
}
