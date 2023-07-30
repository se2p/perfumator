package de.jsilbereisen.perfumator.engine.detector.util;

import com.github.javaparser.ast.Modifier;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMethodDeclarationMatcher implements MethodDeclarationMatcher {

    /**
     * Expected access modifier for the declaration to match. If you don't want to match the visibility modifier, set
     * this to {@code null}.
     */
    @Nullable
    protected final Modifier.Keyword visibilityModifier;

    /**
     * Expected name of the method. If you don't want to match the method-name, set this to {@code null}.
     */
    @Nullable
    protected final String name;

    protected AbstractMethodDeclarationMatcher(@Nullable Modifier.Keyword visibilityModifier, @Nullable String name) {
        this.visibilityModifier = visibilityModifier;
        this.name = name;
    }
}
