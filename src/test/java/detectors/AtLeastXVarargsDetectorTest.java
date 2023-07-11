package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.AtLeastXVarargsDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AtLeastXVarargsDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new AtLeastXVarargsDetector();

    private static final Path TEST_FILE = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("VarargsPerfume.java");

    private static Perfume perfume;

    private static CompilationUnit ast;

    @BeforeAll
    static void getAst() {
        ast = parseAstForFile(TEST_FILE);

        perfume = new Perfume();
        perfume.setName("At least X varargs");
        DETECTOR.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfume() {
        List<DetectedInstance<Perfume>> detections = DETECTOR.detect(ast);

        assertThat(detections).hasSize(2);
        detections.sort(DetectedInstance::compareTo);

        DetectedInstance<Perfume> detected = detections.get(0);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("Inner");
        assertThat(detected.getBeginningLineNumber()).isEqualTo(13);
        assertThat(detected.getEndingLineNumber()).isEqualTo(16);
        assertThat(detected.getConcreteCode()).isNotEmpty();

        detected = detections.get(1);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("VarargsPerfume");
        assertThat(detected.getBeginningLineNumber()).isEqualTo(6);
        assertThat(detected.getEndingLineNumber()).isEqualTo(9);
        assertThat(detected.getConcreteCode()).isNotEmpty();
    }

}