package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.EqualsAndHashCodePairDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EqualsAndHashCodePairDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new EqualsAndHashCodePairDetector();

    private static Perfume perfume;

    private static CompilationUnit ast;

    @BeforeAll
    static void init() {
        perfume = new Perfume();
        perfume.setName("Paired \"equals\" and \"hashCode\"");
        DETECTOR.setConcreteDetectable(perfume);
        ast = parseAstForFile(DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("PairEqualsAndHashCode.java"));
    }

    @Test
    void detectPerfume() {
        List<DetectedInstance<Perfume>> detections = DETECTOR.detect(ast);

        assertThat(detections).hasSize(2);

        detections.sort(DetectedInstance::compareTo);

        // Check first
        DetectedInstance<Perfume> detected = detections.get(0);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("PairEqualsAndHashCode");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(32, 5, 32, 54),
                CodeRange.of(33, 5, 33, 40));

        // Check second
        detected = detections.get(1);
        assertThat(detected.getDetectable()).isEqualTo(perfume);
        assertThat(detected.getTypeName()).isEqualTo("Perfumed");
        assertThat(detected.getCodeRanges()).containsExactly(CodeRange.of(6, 9, 6, 58),
                CodeRange.of(7, 9, 7, 41));
    }
}
