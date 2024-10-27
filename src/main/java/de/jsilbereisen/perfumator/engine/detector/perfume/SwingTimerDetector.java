package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
 * {@link Detector} for the "Swing timer" {@link Perfume}.
 * Detects the perfume only if an object of type {@link javax.swing.Timer} is created.
 */
public class SwingTimerDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String QUALIFIED_TIMER_NAME = "javax.swing.Timer";
    private static final String TIMER_IDENTIFIER = "Timer";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<ObjectCreationExpr> newTimerExpressions = getNewTimerExpressions(astRoot);
        newTimerExpressions
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
    
    private List<ObjectCreationExpr> getNewTimerExpressions(@NotNull CompilationUnit astRoot) {
        return astRoot.findAll(ObjectCreationExpr.class, expr -> {
            if (!expr.getType().getNameAsString().equals(TIMER_IDENTIFIER)) {
                return false;
            }
            ResolvedType resolvedType;
            // when classes from the same package are instantiated, the javaparser cannot resolve the type, therefore
            // we simply skip such occurrences of object creation expressions
            try {
                resolvedType = expr.calculateResolvedType();
            } catch (Exception e) {
                System.out.println(expr);
                System.out.println(e.getMessage());
                return false;
            }
            return resolvedType instanceof ReferenceTypeImpl referenceType 
                    && referenceType.getQualifiedName().equals(QUALIFIED_TIMER_NAME);
        });
    }
}
