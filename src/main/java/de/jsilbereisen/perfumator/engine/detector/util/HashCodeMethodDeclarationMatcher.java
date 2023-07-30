package de.jsilbereisen.perfumator.engine.detector.util;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.Collections;

/**
 * {@link MethodDeclarationMatcher} for the {@link Object#hashCode()} method.
 */
public class HashCodeMethodDeclarationMatcher extends PrimitiveMethodDeclarationMatcher {

    public static final String HASH_CODE_NAME = "hashCode";

    public HashCodeMethodDeclarationMatcher() {
        super(Modifier.Keyword.PUBLIC, PrimitiveType.intType(), HASH_CODE_NAME, Collections.emptyList());
    }
}
