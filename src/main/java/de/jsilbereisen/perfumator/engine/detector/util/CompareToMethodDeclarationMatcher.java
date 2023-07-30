package de.jsilbereisen.perfumator.engine.detector.util;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.List;

/**
 * <p>
 * {@link MethodDeclarationMatcher} for the {@link Comparable#compareTo} method.
 * Does not match the parameter's type.
 * </p>
 * <p>
 * <b>Note:</b> The method's parameter type is not checked, so it is not exactly verified,
 * that a method actually overrides the {@link Comparable#compareTo} method with the correct type parameter.
 * So, in rare cases where the defined {@code compareTo} method is not the one that overrides the interface's
 * method, because it has another parameter type (overload), this can produce wrong results. The verification is
 * left out because of this case happening being very unlikely in my opinion, so we don't need to exhaustively
 * resolve symbols.
 * </p>
 */
public class CompareToMethodDeclarationMatcher extends PrimitiveMethodDeclarationMatcher {

    public static final String COMPARE_TO_NAME = "compareTo";

    public CompareToMethodDeclarationMatcher() {
        super(Modifier.Keyword.PUBLIC, PrimitiveType.intType(), COMPARE_TO_NAME, null);
    }
}
