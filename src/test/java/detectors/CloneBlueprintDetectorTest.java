package detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.AbstractDetectorTest;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.CloneBlueprintDetector;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloneBlueprintDetectorTest extends AbstractDetectorTest {

    private static final Path TEST_FILES_DIR = DEFAULT_DETECTOR_TEST_FILES_DIR.resolve("clone_blueprint");

    private static Perfume perfume;

    private Detector<Perfume> detector;

    @BeforeAll
    static void setupPerfume() {
        perfume = new Perfume();
        perfume.setName("Clone blueprint");
    }

    @BeforeEach
    void setupDetector() {
        // Only need reflection resolution in this case
        detector = new CloneBlueprintDetector();
        detector.setConcreteDetectable(perfume);
    }

    @Test
    void detect() {
        JavaParserFacade analysisContext = getAnalysisContext(parser);
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, TEST_FILES_DIR.resolve("CloneBlueprintPerfume.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(2);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("Perfumed");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(7, 9, 10, 9));

        detection = detections.get(1);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("AlsoPerfumed");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(15, 9, 19, 9));
    }

    @Test
    void detectWithDependencies() {
        Path testDir = TEST_FILES_DIR.resolve("with_dependencies");

        JavaParserFacade analysisContext = getAnalysisContext(parser, testDir.resolve("dependency"));
        detector.setAnalysisContext(analysisContext);

        CompilationUnit ast = parseAstForFile(parser, testDir.resolve("PerfumeWithDependency.java"));

        List<DetectedInstance<Perfume>> detections = detector.detect(ast);

        assertThat(detections).hasSize(1);

        DetectedInstance<Perfume> detection = detections.get(0);
        assertThat(detection.getDetectable()).isEqualTo(perfume);
        assertThat(detection.getTypeName()).isEqualTo("Perfumed");
        assertThat(detection.getCodeRanges()).containsExactly(CodeRange.of(9, 9, 12, 9));
    }
}
