package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.Perfume;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        return List.of(new DetectedInstance<>(perfume, astRoot.getPrimaryTypeName().orElse("No type name found"), 1,
                "public class SomeClassName { }"));
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) { }
}
