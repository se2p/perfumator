package detectors;

import com.github.javaparser.ast.CompilationUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.NoUtilityInitializationDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.DetectedInstanceComparator;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

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
        assertThat(detectedInstance.getCodeRanges()).containsExactly(CodeRange.of(8, 1, 19, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NoMethods.java", "ImplicitConstructor.java", "ExplicitPublicConstructor.java",
            "NonStaticMethod.java"})
    void notPerfumed(@NotNull String testFileName) {
        Path testFile = TEST_FILES_DIR.resolve(testFileName);
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
        assertThat(detectedInstance.getCodeRanges()).containsExactly(CodeRange.of(9, 1, 22, 1));
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
        assertThat(perfumedInnerClass.getCodeRanges()).containsExactly(CodeRange.of(10, 5, 16, 5));

        assertThat(perfumedStaticInnerClass.getDetectable()).isEqualTo(perfume);
        assertThat(perfumedStaticInnerClass.getTypeName()).isEqualTo("PerfumedStaticInnerClass");
        assertThat(perfumedStaticInnerClass.getCodeRanges()).containsExactly(CodeRange.of(18, 5, 23, 5));
    }
}
