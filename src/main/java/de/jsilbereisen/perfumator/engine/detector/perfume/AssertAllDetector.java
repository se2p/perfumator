package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Detector} for the "Assert all" {@link Perfume}.
 * Detects the perfume only if the method is part of the {@link org.junit.jupiter.api.Assertions} class.
 */
@EqualsAndHashCode
public class AssertAllDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String QUALIFIED_ASSERT_ALL_METHOD_NAME = "org.junit.jupiter.api.Assertions.assertAll";
    private static final String ASSERTIONS_IMPORT_NAME = "org.junit.jupiter.api.Assertions";
    private static final String ASSERT_ALL = "assertAll";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodCallExpr> assertAllMethodCallExpressions = getAssertAllMethodCalls(astRoot);
        assertAllMethodCallExpressions
                .forEach(callExpr -> detectedInstances.add(DetectedInstance.from(callExpr, perfume, astRoot)));
        return detectedInstances;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        this.perfume = concreteDetectable;
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
        this.analysisContext = analysisContext;
    }

    private List<MethodCallExpr> getAssertAllMethodCalls(@NotNull CompilationUnit astRoot) {
        return astRoot.findAll(MethodCallExpr.class, expr -> {
            // contains instead of equals because of possible 'Assertions.assertAll' calls
            if (!expr.getNameAsString().contains(ASSERT_ALL)) {
                return false;
            }
            if (expr.getScope().isPresent()) {
                // for non-static imports
                ResolvedType resolvedType = expr.getScope().get().calculateResolvedType();
                return resolvedType instanceof ReferenceTypeImpl referenceType 
                        && referenceType.getQualifiedName().equals(ASSERTIONS_IMPORT_NAME);
            } else {
                // for static imports
                ResolvedMethodDeclaration resolvedMethodDeclaration = expr.resolve();
                String qualifiedName = resolvedMethodDeclaration.getQualifiedName();
                return qualifiedName.equals(QUALIFIED_ASSERT_ALL_METHOD_NAME);
            }
        });
    }
}
