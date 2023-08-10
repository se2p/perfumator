package test.dummy;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;

/**
 * Dummy detector to test auto-instantiation of Perfume detectors.
 */
@EqualsAndHashCode
public class DummyDetector implements Detector<Perfume> {

    private final Perfume perfume;

    public DummyDetector() {
        perfume = new Perfume();
        perfume.setName("Some Perfume");
    }

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        return List.of(new DetectedInstance<>(perfume, astRoot.getPrimaryTypeName().orElse("No type name found"), 1, 1,
                Path.of("somewhere/over/the/rainbow")));
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
    }
}
