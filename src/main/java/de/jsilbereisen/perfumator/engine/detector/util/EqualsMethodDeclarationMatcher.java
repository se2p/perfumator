package de.jsilbereisen.perfumator.engine.detector.util;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.List;

/**
 * <p>{@link MethodDeclarationMatcher} for the {@link Object#equals} method.</p>
 * <p>
 * <b>Note</b> that the parameter type is only validated by its name, so this
 * produces false positives if there is an {@code equals} method which has a single parameter of type {@code
 * Object}, but which is not {@code java.lang.Object}.
 * </p>
 *
 * @// Note: 30.07.2023  This could be fixed by additional symbol resolution of the type, but I see it as very
 * unlikely that this edge case happens, so it's left out for now.
 * @// Note 24.10.2023: Would not recommend symbol resolution in this case, because I see the danger of not being able
 * to resolve the class because of missing dependencies higher than the danger of someone defining a custom "equals(Object)"
 * method, where Object is <i>not</i> java.lang.Object, as quite low.
 */
public class EqualsMethodDeclarationMatcher extends PrimitiveMethodDeclarationMatcher {

    public static final String EQUALS_NAME = "equals";

    public static final String OBJECT_CLASS_NAME = "Object";

    public EqualsMethodDeclarationMatcher() {
        super(Modifier.Keyword.PUBLIC, PrimitiveType.booleanType(), EQUALS_NAME, List.of(OBJECT_CLASS_NAME));
    }
}
