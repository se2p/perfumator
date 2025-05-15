package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.NodeUtil;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link Detector} for the "Assert all" {@link Perfume}.
 * Detects the perfume only if the method is part of the {@link org.junit.jupiter.api.Assertions} class.
 */
@EqualsAndHashCode
public class AssertAllDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String DECLARING_CLASS = "org.junit.jupiter.api.Assertions";
    private static final String ASSERT_ALL = "assertAll";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodCallExpr> assertAllMethodCallExpressions = getAssertAllMethodCalls(astRoot);
        assertAllMethodCallExpressions.forEach(callExpr ->
                detectedInstances.add(DetectedInstance.from(callExpr, perfume, astRoot)));
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
        return astRoot.findAll(MethodCallExpr.class).stream()
                .filter(expr -> expr.getNameAsString().equals(ASSERT_ALL))
                .filter(expr -> {
                    ResolvedMethodDeclaration methodDeclaration;
                    try {
                        methodDeclaration = analysisContext.solve(expr).getCorrespondingDeclaration();
                    } catch (UnsupportedOperationException e) {
                        e.printStackTrace();
                        return false;
                    }
                    ResolvedReferenceTypeDeclaration referenceType = methodDeclaration.declaringType().asReferenceType();
                    return DECLARING_CLASS.equals(referenceType.getQualifiedName());
                }).toList();
    }
}
