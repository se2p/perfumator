package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
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
 * Detects the perfume only if the method is part of the {@link java.awt.Window} class.
 */
public class JFrameDisposeDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String DISPOSE_METHOD_NAME = "dispose";
    private static final String DECLARING_CLASS = "java.awt.Window";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodCallExpr> disposeMethodCallExpressions = getJFrameDisposeMethodCalls(astRoot);
        disposeMethodCallExpressions.forEach(expr ->
                detectedInstances.add(DetectedInstance.from(expr, perfume, astRoot)));
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
        return astRoot.findAll(MethodCallExpr.class).stream()
                // only consider methods with name "dispose"
                .filter(expr -> expr.getNameAsString().equals(DISPOSE_METHOD_NAME))
                // ensure that the declaring class is JFrame
                .filter(expr -> {
                    ResolvedMethodDeclaration disposeDeclaration;
                    try {
                        disposeDeclaration = analysisContext.solve(expr).getCorrespondingDeclaration();
                    } catch (UnsupportedOperationException e) {
                        e.printStackTrace();
                        return false;
                    }
                    var referenceType = disposeDeclaration.declaringType().asReferenceType();
                    if (referenceType.getQualifiedName().equals(DECLARING_CLASS)) {
                        return true;
                    } else {
                        return referenceType.getAllAncestors().stream()
                                .anyMatch(ancestor ->
                                        ancestor.getQualifiedName().equals(DECLARING_CLASS));
                    }
                }).toList();
    }
}
