package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.AtLeastXVarargsDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.dummy.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AtLeastXVarargsDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new AtLeastXVarargsDetector();

    private static final Path TEST_FILE = Path.of("src", "test", "resources", "detectors", "VarargsPerfume.java");

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
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(1);
        assertThat(detected.get(0).getDetectable()).isEqualTo(perfume);
        assertThat(detected.get(0).getBeginningLineNumber()).isEqualTo(6);
        assertThat(detected.get(0).getEndingLineNumber()).isEqualTo(9);
        assertThat(detected.get(0).getParentTypeName()).isEqualTo("VarargsPerfume");
        assertThat(detected.get(0).getConcreteCode()).isNotEmpty();
    }

}