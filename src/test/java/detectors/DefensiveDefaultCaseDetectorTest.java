package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.DefensiveDefaultCaseDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefensiveDefaultCaseDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new DefensiveDefaultCaseDetector();

    private static final Path TEST_FILE = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("DefensiveDefaultCase.java");

    private static Perfume perfume;

    private static CompilationUnit ast;

    @BeforeAll
    static void getAst() {
        ast = parseAstForFile(TEST_FILE);

        perfume = new Perfume();
        perfume.setName("Defensive Default case");
        DETECTOR.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfume() {
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(2);

        DetectedInstance<Perfume> first = detected.get(0);
        assertThat(first.getDetectable()).isEqualTo(perfume);
        assertThat(first.getTypeName()).isEqualTo("DefensiveDefaultCase");
        assertThat(first.getBeginningLineNumber()).isEqualTo(11);
        assertThat(first.getEndingLineNumber()).isEqualTo(20);
    }
}
