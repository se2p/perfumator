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
    private final static Set<String> QUALIFIED_METHOD_NAMES 
            = Set.of("javax.swing.SwingUtilities.invokeLater", "javax.swing.SwingUtilities.invokeAndWait");
    private final static String IMPORT = "javax.swing.SwingUtilities";
    
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
        return astRoot.findAll(MethodCallExpr.class, expr -> {
            // contains instead of equals because of possible 'SwingUtilities.invokeLater' and '-.invokeAndWait' calls
            if (!expr.getNameAsString().contains(INVOKE_LATER) && !expr.getNameAsString().contains(INVOKE_AND_WAIT)) {
                return false;
            }
            if (expr.getScope().isPresent()) {
                // for non-static imports
                ResolvedType resolvedType;
                try {
                    resolvedType = expr.getScope().get().calculateResolvedType();
                } catch (Exception e) {
                    System.out.println(expr.getNameAsString());
                    System.out.println(e.getMessage());
                    return false;
                }
                return resolvedType instanceof ReferenceTypeImpl referenceType
                        && referenceType.getQualifiedName().equals(IMPORT);
            } else {
                // for static imports
                ResolvedMethodDeclaration resolvedMethodDeclaration = expr.resolve();
                String qualifiedName = resolvedMethodDeclaration.getQualifiedName();
                return QUALIFIED_METHOD_NAMES.contains(qualifiedName);
            }
        });
    }
}
