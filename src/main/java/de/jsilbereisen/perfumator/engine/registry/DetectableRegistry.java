package de.jsilbereisen.perfumator.engine.registry;

import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Interface for a registry that loads and stores {@link T} {@link Detectable}s and links them with their
 * respective {@link Detector<T>}.<br/>
 * Every registered {@link T} <b>MUST</b> have a linked {@link Detector<T>},
 * so there must be <b>NO CASE</b> in which a call to {@code getRegisteredDetectables().contains(T)} with a
 * concrete {@link T} gives {@code true} but {@link #getDetector(T)} with the same {@link T} results in {@code null},
 * with no calls to {@link #loadRegistry} in between.
 *
 * @param <T> The concrete type of {@link Detectable} that is loaded and stored by this registry.
 */
public interface DetectableRegistry<T extends Detectable> {

    /**
     * <p>
     * Loads all relevant {@link Detectable}s with type {@link T} into the registry and links them with their
     * respective {@link Detector<T>}. Performs internationalization on the {@link T} with the given
     * {@link Locale}.
     * </p>
     * <p>
     * Implementations must ensure to throw appropriate exceptions if loading any desired {@link T} fails
     * or if for a loaded {@link T}, no {@link Detector<T>} can be instantiate/loaded!
     * Every loaded {@link T} <b>MUST</b> have a linked {@link Detector<T>}.
     * </p>
     *
     * @param locale {@link Locale} to use for internationalization.
     */
    void loadRegistry(@NotNull Locale locale);

    /**
     * Returns a list of all {@link T} {@link Detectable}s that are currently loaded in the registry.
     * If none are loaded, maybe because no call to {@link #loadRegistry} was issued yet,
     * returns an empty list.
     *
     * @return List of {@link T}s that are currently registered.
     */
    @NotNull List<T> getRegisteredDetectables();

    /**
     * Returns a list of all {@link Detector<T>}s that are currently loaded in the registry.
     * If none are loaded, maybe because no call to {@link #loadRegistry} was issued yet,
     * returns an empty list.
     *
     * @return List of {@link Detector<T>}s that are currently registered.
     */
    @NotNull List<Detector<T>> getRegisteredDetectors();

    /**
     * Returns the {@link Detector<T>} that is linked to the given {@link T} {@link Detectable}.
     * If {@code null} is returned, this must mean that the {@link T} is unknown to this {@link DetectableRegistry<T>},
     * so either there was no call to {@link #loadRegistry} yet, or the given {@link T} is generally not in the scope
     * of {@link T}s that is loaded by {@link #loadRegistry}.
     *
     * @param detectable The {@link T} {@link Detectable} for which the linked {@link Detector<T>} is searched.
     * @return The linked {@link Detector<T>}.
     */
    @Nullable Detector<T> getDetector(@NotNull T detectable);
}
