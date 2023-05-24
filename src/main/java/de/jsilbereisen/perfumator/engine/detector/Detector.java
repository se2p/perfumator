package de.jsilbereisen.perfumator.engine.detector;

import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for a {@link Detector} for a type {@link T} that is a Detectable.
 *
 * @param <T> Type of detectable that is to be detected.
 */
public interface Detector<T extends Detectable> {

    /**
     * Searches for and detects instances of {@link T} in the AST, given by the root node.
     *
     * @param astRoot The root node of the AST where the {@link T} should be detected.
     * @return A {@link List} with all {@link DetectedInstance <T>}s of {@link T}. If no instances
     *         are found, returns an empty list.
     */
    @NotNull List<DetectedInstance<T>> detect(@NotNull CompilationUnit astRoot);
}
