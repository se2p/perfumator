package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.TryWithResourcesDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TryWithResourcesDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILE = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("UseTryWithResources.java");

    private static final Detector<Perfume> DETECTOR = new TryWithResourcesDetector();

    private static CompilationUnit ast;

    private static Perfume perfume;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        DETECTOR.setConcreteDetectable(perfume);
        ast = parseAstForFile(TEST_FILE);
    }

    @Test
    void detectTryWithResources() {
        List<DetectedInstance<Perfume>> detections = DETECTOR.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("UseTryWithResources");
        assertThat(detection.getBeginningLineNumber()).isEqualTo(6);
        assertThat(detection.getEndingLineNumber()).isEqualTo(10);
    }
}
