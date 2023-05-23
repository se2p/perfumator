package de.jsilbereisen.perfumator.data.loader;

import de.jsilbereisen.perfumator.data.model.Detectable;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Interface for a registry that loads and stores {@link Detectable}s and links them with their
 * respective {@link Detector}.
 *
 * @param <T> The type of {@link Detectable} that is loaded and stored by this registry.
 */
public interface DetectableRegistry<T extends Detectable> {

    void loadRegistry(@NotNull Locale locale);

    // TODO Make it throw exception when no call to loadRegistry happened yet? (e.g. Registry is empty)
    List<T> getRegisteredDetectables();

    List<Detector<T>> getRegisteredDetectors();

    Detector<T> getDetector(T detectable);
}
