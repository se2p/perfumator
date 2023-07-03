package de.jsilbereisen.perfumator.engine.detector;

import com.github.javaparser.ast.CompilationUnit;
import org.jetbrains.annotations.NotNull;

import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;

import java.util.List;

/**
 * Interface for a {@link Detector} for a type {@link T} that is a Detectable.
 * Implementations must override {@link Object#equals} and {@link Object#hashCode},
 * to allow {@link de.jsilbereisen.perfumator.engine.registry.DetectableRegistry} implementations
 * to conveniently use {@link java.util.Set}s for their return types.
 *
 * @param <T> Type of detectable that is to be detected.
 */
public interface Detector<T extends Detectable> {

    /**
     * Searches for and detects instances of {@link T} in the AST, given by the root node.
     *
     * @param astRoot The root node of the AST in which the {@link T} should be searched for.
     * @return A {@link List} with all {@link DetectedInstance <T>}s of {@link T}. If no instances
     * are found, returns an empty list.
     */
    @NotNull List<DetectedInstance<T>> detect(@NotNull CompilationUnit astRoot);

    /**
     * Sets the concrete {@link T} instance that is detected by this {@link Detector<T>}.
     * This might for example be the specific Perfume that this detector is responsible for.
     *
     * @param concreteDetectable The {@link Detectable} that this detector detects.
     */
    void setConcreteDetectable(@NotNull T concreteDetectable);

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
