package detectors;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.NoUtilityInitializationDetector;
import de.jsilbereisen.perfumator.model.*;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoUtilityInitializationDetectorTest extends AbstractDetectorTest {

    private static final Detector<Perfume> DETECTOR = new NoUtilityInitializationDetector();

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("no_utility_initialization");

    private static Perfume perfume;

    @BeforeAll
    static void setup() {
        perfume = new Perfume();
        perfume.setName("No utility initialization");
        DETECTOR.setConcreteDetectable(perfume);
    }

    @Test
    void detectPerfume() {
        Path testFile = TEST_FILES_DIR.resolve("NoUtilityInitializationPerfume.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(1);

        DetectedInstance<Perfume> detectedInstance = detected.get(0);
        assertThat(detectedInstance.getDetectable()).isEqualTo(perfume);
        assertThat(detectedInstance.getTypeName()).isEqualTo("NoUtilityInitializationPerfume");
        assertThat(detectedInstance.getBeginningLineNumber()).isEqualTo(8);
        assertThat(detectedInstance.getEndingLineNumber()).isEqualTo(19);
    }

    @Test
    void notPerfumedWhenHasNoDeclaredMethods() {
        Path testFile = TEST_FILES_DIR.resolve("NoMethods.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).isEmpty();
    }

    @Test
    void notPerfumedImplicitConstructor() {
        Path testFile = TEST_FILES_DIR.resolve("ImplicitConstructor.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).isEmpty();
    }

    @Test
    void notPerfumedExplicitPublicConstructor() {
        Path testFile = TEST_FILES_DIR.resolve("ExplicitPublicConstructor.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).isEmpty();
    }

    @Test
    void notPerfumedNonStaticMethod() {
        Path testFile = TEST_FILES_DIR.resolve("NonStaticMethod.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).isEmpty();
    }

    @Test
    void detectPerfumeWithInnerClass() {
        Path testFile = TEST_FILES_DIR.resolve("WithInnerClass.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(1);

        DetectedInstance<Perfume> detectedInstance = detected.get(0);
        assertThat(detectedInstance.getDetectable()).isEqualTo(perfume);
        assertThat(detectedInstance.getTypeName()).isEqualTo("WithInnerClass");
        assertThat(detectedInstance.getBeginningLineNumber()).isEqualTo(9);
        assertThat(detectedInstance.getEndingLineNumber()).isEqualTo(22);
    }

    @Test
    void detectPerfumedInnerClasses() {
        Path testFile = TEST_FILES_DIR.resolve("InnerClassPerfumed.java");
        CompilationUnit ast = parseAstForFile(testFile);
        List<DetectedInstance<Perfume>> detected = DETECTOR.detect(ast);

        assertThat(detected).hasSize(2);

        // Ensure consistent test execution
        detected.sort(new DetectedInstanceComparator<>());

        DetectedInstance<Perfume> perfumedInnerClass = detected.get(0);
        DetectedInstance<Perfume> perfumedStaticInnerClass = detected.get(1);

        assertThat(perfumedInnerClass.getDetectable()).isEqualTo(perfume);
        assertThat(perfumedInnerClass.getTypeName()).isEqualTo("PerfumedInnerClass");
        assertThat(perfumedInnerClass.getBeginningLineNumber()).isEqualTo(10);
        assertThat(perfumedInnerClass.getEndingLineNumber()).isEqualTo(16);

        assertThat(perfumedStaticInnerClass.getDetectable()).isEqualTo(perfume);
        assertThat(perfumedStaticInnerClass.getTypeName()).isEqualTo("PerfumedStaticInnerClass");
        assertThat(perfumedStaticInnerClass.getBeginningLineNumber()).isEqualTo(18);
        assertThat(perfumedStaticInnerClass.getEndingLineNumber()).isEqualTo(23);
    }
}
