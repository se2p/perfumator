package de.jsilbereisen.perfumator.util;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class for {@link Node}s from the <i>JavaParser</i> AST library.
 */
@Slf4j
public final class NodeUtil {

    private NodeUtil() {
    }

    /**
     * Casts the given {@link Node} to the type given by the {@link Class} parameter, if possible.
     * Otherwise returns {@code null}.
     *
     * @param node    The {@link Node} of interest.
     * @param asClass Class object of the desired type.
     * @param <T>     The desired type.
     * @return The given {@link Node}, casted to type {@link T} if possible, or {@code null} otherwise.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Node> T as(@NotNull Node node, @NotNull Class<T> asClass) {
        if (asClass.isAssignableFrom(node.getClass())) {
            return (T) node;
        }

        return null;
    }

    /**
     * Casts the given {@link ResolvedType} to the type given by the {@link Class} parameter, if possible.
     * Otherwise returns {@code null}.
     *
     * @param node    The {@link ResolvedType} of interest.
     * @param asClass Class object of the desired type.
     * @param <T>     The desired type.
     * @return The given {@link ResolvedType}, casted to type {@link T} if possible, or {@code null} otherwise.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends ResolvedType> T as(@NotNull ResolvedType node, @NotNull Class<T> asClass) {
        if (asClass.isAssignableFrom(node.getClass())) {
            return (T) node;
        }

        return null;
    }

    /**
     * Casts the given {@link Node} to the type given by the {@link Class} parameter, if possible,
     * and returns the cast result.
     * Otherwise, returns the result of {@link Supplier#get()}.
     *
     * @param node    The {@link Node} of interest.
     * @param asClass Class object of the desired type.
     * @param orElse  {@link Supplier} that provides a {@link T} if the cast is not possible.
     * @param <T>     The desired type.
     * @return The given {@link Node}, casted to type {@link T} if possible, or {@link Supplier#get()} otherwise,
     * which might be {@code null}.
     */
    @Nullable
    public static <T extends Node> T asOrElse(@NotNull Node node, @NotNull Class<T> asClass,
                                              @NotNull Supplier<T> orElse) {
        T casted = as(node, asClass);

        return casted != null ? casted : orElse.get();
    }

    /**
     * <p>
     * Returns the name of the Node with all its type Parameters appended.
     * E.g.: A class declaration "{@code class MyClass<T extends Node, S>}"
     * results in "{@code MyClass<T,S>}".
     * </p><p>
     * The implementation is meant to give similar output to {@link ClassOrInterfaceType#asString()},
     * so there are no spaces within the brackets.
     * </p>
     *
     * @param node The {@link Node} to print.
     * @param <T>  A node that can have type parameters and that has a simple name.
     * @return The type name with all type parameters, if it has any.
     */
    @NotNull
    public static <T extends Node & NodeWithTypeParameters<T> & NodeWithSimpleName<T>> String getNameWithTypeParams(@NotNull T node) {
        StringBuilder builder = new StringBuilder(node.getName().getIdentifier());

        NodeList<TypeParameter> typeParameters = node.getTypeParameters();
        if (typeParameters.isNonEmpty()) {
            builder.append("<");

            for (int i = 0; i < typeParameters.size(); i++) {
                builder.append(typeParameters.get(i).getName().getIdentifier());
                if (i < typeParameters.size() - 1) {
                    builder.append(",");
                }
            }

            builder.append(">");
        }

        return builder.toString();
    }

    /**
     * Resolves a class declaration from the standard library. If resolving fails, logs the given error message.
     * As this should never fail if the given {@link JavaParserFacade} has a well-configured {@link TypeSolver}, like a
     * {@link CombinedTypeSolver} that has a {@link ReflectionTypeSolver}, or directly a {@link ReflectionTypeSolver},
     * and if the given qualified class name is indeed a class of the JDK, this method re-throws then the
     * {@link UnsolvedSymbolException}.
     *
     * @param qualifiedClassName The fully qualified class name of the class to resolve from the JDK.
     * @param configuredContext A context, that has a {@link TypeSolver} that is able to resolve JDK classes.
     * @param unresolvedMessage Message to log when resolving fails.
     * @return The resolved type declaration.
     * @throws UnsolvedSymbolException after logging the given error message, if the class could not be resolved.
     */
    @NotNull
    public static ResolvedReferenceTypeDeclaration resolveFromStandardLibrary(
            @NotNull String qualifiedClassName, @NotNull JavaParserFacade configuredContext, @NotNull String unresolvedMessage)
            throws UnsolvedSymbolException {
        if (!qualifiedClassName.startsWith("java.") && !qualifiedClassName.startsWith("javax.")
                && !qualifiedClassName.startsWith("jakarta.")) {
            throw new IllegalArgumentException("The class package name does not match the standard library.");
        }

        try {
            return configuredContext.getTypeSolver().solveType(qualifiedClassName);
        } catch (UnsolvedSymbolException e) {
            log.error(unresolvedMessage);
            throw new UnsolvedSymbolException(e.getMessage(), e);
        }
    }

    /**
     * Tries to resolve the given {@link Resolvable}. If not possible, logs a generic debug message including the given
     * "name" (e.g. Method call, variable name) and the given {@link Detector}s name, and returns {@link Optional#empty()}.
     * If successful, returns an {@link Optional} with the resolved {@link T}.
     *
     * @param resolvable To resolve.
     * @param detector The {@link Detector} who runs the analysis, where the given {@link Resolvable} should be resolved.
     *                      Mainly for debugging.
     * @param nameOfResolvable "Name" of the resolvable, but can be anything that helps, mainly for debugging.
     * @return An {@link Optional} with the resolved {@link T} if successful, {@link Optional#empty()} otherwise.
     * @param <T> The result-type of the resolution.
     */
    @NotNull
    public static  <T> Optional<T> resolveSafely(@NotNull Resolvable<T> resolvable, @NotNull Detector<?> detector,
                                                 @NotNull String nameOfResolvable) {
        T resolved;

        try {
            resolved = resolvable.resolve();
        } catch (UnsolvedSymbolException e) {
            log.debug("Detector \"" + detector.getClass().getSimpleName() + "\" could not resolve the symbol \""
                    + nameOfResolvable + "\" in the provided context.", e);
            return Optional.empty();
        } catch (Exception e) {
            log.debug("Exception while resolving symbol: Detector \"" + detector.getClass().getSimpleName()
                    + ", Symbol \"" + nameOfResolvable + "\".", e);
            return Optional.empty();
        }

        return Optional.ofNullable(resolved);
    }
}
