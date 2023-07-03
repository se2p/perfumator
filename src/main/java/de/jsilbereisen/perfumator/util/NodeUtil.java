package de.jsilbereisen.perfumator.util;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Utility class for {@link Node}s from the <i>JavaParser</i> AST library.
 */
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

}
