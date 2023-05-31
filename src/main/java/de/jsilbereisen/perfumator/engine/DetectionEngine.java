package de.jsilbereisen.perfumator.engine;

import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public interface DetectionEngine<T extends Detectable> {

    List<DetectedInstance<T>> detect(@NotNull Path sources);
}
