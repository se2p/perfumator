package de.jsilbereisen.perfumator.engine.detector.util;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PrimitiveMethodDeclarationMatcher extends AbstractMethodDeclarationMatcher {

    /**
     * The expected return type of the method. If you don't want to check the return-type, set this to {@code null}.
     */
    @Nullable
    protected final Type returnType;

    /**
     * Expected type names, including type parameters, of the parameters. Must be in the correct order.
     * E.g. a list of "Function&lt;Integer,Boolean&gt;" and "Object" matches a method
     * <pre>
     *     void method(Function&lt;Integer, Boolean&gt; f, Object o)
     * </pre>
     * signature. If you don't want to check the parameter-types, set this to {@code null}.
     */
    @Nullable
    protected final List<String> parameterTypes;

    protected PrimitiveMethodDeclarationMatcher(@Nullable Modifier.Keyword visibilityModifier, @Nullable Type returnType,
                                                @Nullable String name, @Nullable List<String> parameterTypes) {
        super(visibilityModifier, name);

        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean matches(@NotNull MethodDeclaration method) {
        if ((visibilityModifier != null && !method.hasModifier(visibilityModifier))
                || (returnType != null && !method.getType().equals(returnType))
                || (name != null && !method.getNameAsString().equals(name))) {
            return false;
        }

        if (parameterTypes != null) {
            List<Parameter> params = method.getParameters();

            if (params.size() != parameterTypes.size()) {
                return false;
            }

            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).getType().asString().equals(parameterTypes.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }
}
