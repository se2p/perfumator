package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * {@link Detector} for the "Thread safe Swing" {@link Perfume}.
 * Detects method calls to the {@link javax.swing.SwingUtilities#invokeAndWait(Runnable)} and 
 * {@link javax.swing.SwingUtilities#invokeLater(Runnable)} methods.
 */
@EqualsAndHashCode
public class ThreadSafeSwingDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    private final static String INVOKE_LATER = "invokeLater";
    private final static String INVOKE_AND_WAIT = "invokeAndWait";
    private final static String DECLARING_CLASS = "javax.swing.SwingUtilities";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodCallExpr> methodCalls = getInvokeLaterInvokeAndWaitMethodCalls(astRoot);
        methodCalls.forEach(callExpr -> detectedInstances.add(DetectedInstance.from(callExpr, perfume, astRoot)));
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
    
    private List<MethodCallExpr> getInvokeLaterInvokeAndWaitMethodCalls(@NotNull CompilationUnit astRoot) {
        return astRoot.findAll(MethodCallExpr.class).stream()
                .filter(expr -> Set.of(INVOKE_AND_WAIT, INVOKE_LATER).contains(expr.getNameAsString()))
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
