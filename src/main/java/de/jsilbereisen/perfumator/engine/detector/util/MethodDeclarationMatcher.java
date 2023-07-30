package de.jsilbereisen.perfumator.engine.detector.util;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for a matcher, that matches the signature of a {@link MethodDeclaration} against
 * an expected signature.
 */
public interface MethodDeclarationMatcher {

    /**
     * Matches the signature of the given {@link MethodDeclaration} against some expected, predefined signature.
     *
     * @param method The method of which the declaration should be matched.
     * @return {@code true} if the declaration matches the expectation, {@code false} otherwise.
     */
    boolean matches(@NotNull MethodDeclaration method);
}
