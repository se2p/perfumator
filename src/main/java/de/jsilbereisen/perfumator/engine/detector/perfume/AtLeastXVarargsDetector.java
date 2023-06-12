package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.Perfume;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AtLeastXVarargsDetector implements Detector<Perfume> {
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        return null;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {

    }
}
