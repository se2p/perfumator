package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.EqualsBlueprintDetector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.DetectedInstanceComparator;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EqualsBlueprintDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new EqualsBlueprintDetector();

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("equals_blueprint");

    private static Perfume perfume;

    @BeforeAll
    static void setup() {
        perfume = new Perfume();
        perfume.setName("Equals blueprint");
        DETECTOR.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfume() {
        Path testFile = TEST_FILES_DIR.resolve("EqualsBlueprintPerfume.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(2);

        detected.sort(new DetectedInstanceComparator<>());

        DetectedInstance<Perfume> first = detected.get(0);
        assertThat(first.getDetectable()).isEqualTo(perfume);
        assertThat(first.getTypeName()).isEqualTo("EqualsBlueprintPerfume");
        assertThat(first.getBeginningLineNumber()).isEqualTo(16);
        assertThat(first.getEndingLineNumber()).isEqualTo(26);

        DetectedInstance<Perfume> second = detected.get(1);
        assertThat(second.getDetectable()).isEqualTo(perfume);
        assertThat(second.getTypeName()).isEqualTo("ShouldBeDetected");
        assertThat(second.getBeginningLineNumber()).isEqualTo(30);
        assertThat(second.getEndingLineNumber()).isEqualTo(41);
    }

    @Test
    void detectPerfumeWithPatternMatching() {
        Path testFile = TEST_FILES_DIR.resolve("EqualsBlueprintWithPatternMatching.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(1);

        DetectedInstance<Perfume> ret = detected.get(0);
        assertThat(ret.getDetectable()).isEqualTo(perfume);
        assertThat(ret.getTypeName()).isEqualTo("EqualsBlueprintWithPatternMatching");
        assertThat(ret.getBeginningLineNumber()).isEqualTo(9);
        assertThat(ret.getEndingLineNumber()).isEqualTo(18);
    }

    @Test
    void detectPerfumeWithGenerics() {
        Path testFile = TEST_FILES_DIR.resolve("EqualsBlueprintWithGenerics.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(1);

        DetectedInstance<Perfume> ret = detected.get(0);
        assertThat(ret.getDetectable()).isEqualTo(perfume);
        assertThat(ret.getTypeName()).isEqualTo("EqualsBlueprintWithGenerics");
        assertThat(ret.getBeginningLineNumber()).isEqualTo(9);
        assertThat(ret.getEndingLineNumber()).isEqualTo(17);
    }
}
