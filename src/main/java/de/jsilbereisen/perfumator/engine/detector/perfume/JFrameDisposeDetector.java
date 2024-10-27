package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Detector} for the "JFrame dispose" {@link Perfume}.
 * Detects the perfume only if the method is part of the {@link javax.swing.JFrame} class.
 */
public class JFrameDisposeDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String DISPOSE_METHOD_NAME = "dispose";
    private static final String QUALIFIED_JFRAME_CLASS_NAME = "javax.swing.JFrame";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodCallExpr> disposeMethodCallExpressions = getJFrameDisposeMethodCalls(astRoot);
        disposeMethodCallExpressions
                .forEach(expr -> detectedInstances.add(DetectedInstance.from(expr, perfume, astRoot)));
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

    private List<MethodCallExpr> getJFrameDisposeMethodCalls(@NotNull CompilationUnit astRoot) {
        return astRoot.findAll(MethodCallExpr.class, expr -> {
            if (!expr.getNameAsString().equals(DISPOSE_METHOD_NAME)) {
                return false;
            }
            var scope = expr.getScope();
            if (scope.isPresent()) {
                ResolvedType resolvedType;
                try {
                    resolvedType = scope.get().calculateResolvedType();
                } catch (Exception e) {
                    System.out.println(expr.getNameAsString());
                    System.out.println(e.getMessage());
                    return false;
                }
                if (resolvedType instanceof ReferenceTypeImpl referenceType) {
                    return referenceType.getQualifiedName().equals(QUALIFIED_JFRAME_CLASS_NAME);
                }
            }
            return false;
        });
    }
}
