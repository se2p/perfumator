package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.CopyConstructorDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CopyConstructorDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("copy_constructor");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void setupPerfume() {
        perfume = new Perfume();
        perfume.setName("Copy constructor");
    }

    @BeforeEach
    void setupDetector() {
        detector = new CopyConstructorDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detectWithSimpleFieldCopies() {
        Path testDir = TEST_FILES_DIR.resolve("same_class");
        JavaParserFacade context = getAnalysisContext(parser, testDir);
        CompilationUnit ast = parseAstForFile(parser, testDir.resolve("SameClass.java"));

        detector.setAnalysisContext(context);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SameClass");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(25, 5, 30, 5));
    }

    @Test
    void detectWithFieldCopiesByMethodCalls() {
        Path testDir = TEST_FILES_DIR.resolve("same_class_methods");
        JavaParserFacade context = getAnalysisContext(parser, testDir);
        CompilationUnit ast = parseAstForFile(parser, testDir.resolve("SameClassMethods.java"));

        detector.setAnalysisContext(context);
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SameClassMethods");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(33, 5, 41, 5));
    }

    @Test
    void detectPerfumedVariations() {
        Path testDirPerfumed = TEST_FILES_DIR.resolve(Path.of("variations", "perfumed"));
        Path testDependencies = TEST_FILES_DIR.resolve(Path.of("variations", "dependencies"));

        JavaParserFacade context = getAnalysisContext(parser, testDirPerfumed, testDependencies);
        detector.setAnalysisContext(context);

        // Perfumed variations without dependency usage
        CompilationUnit ast = parseAstForFile(parser, testDirPerfumed.resolve("SameClassVariations.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("SameClassVariations");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(11, 5, 15, 5));

        // Perfumed variations with dependency usage
        ast = parseAstForFile(parser, testDirPerfumed.resolve("DependencyVariations.java"));
        detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("DependencyVariations");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(9, 5, 11, 5));
    }

    @Test
    void notPerfumedVariations() {
        Path testDirNotPerfumed = TEST_FILES_DIR.resolve(Path.of("variations", "not_perfumed"));
        Path testDependencies = TEST_FILES_DIR.resolve(Path.of("variations", "dependencies"));

        JavaParserFacade context = getAnalysisContext(parser, testDirNotPerfumed, testDependencies);
        detector.setAnalysisContext(context);

        // Not perfumed variations without dependency usage
        CompilationUnit ast = parseAstForFile(parser, testDirNotPerfumed.resolve("SameClassVariations.java"));
        List<DetectedInstance<Perfume>> detections = detector.detect(ast);
        assertThat(detections).isEmpty();


        // Not perfumed variations with dependency usage
        ast = parseAstForFile(parser, testDirNotPerfumed.resolve("DependencyVariations.java"));
        detections = detector.detect(ast);
        assertThat(detections).isEmpty();
    }
}
