package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodCallByNameVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
public class AssertAllDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String ASSERT_ALL_METHOD_NAME = "assertAll";
    
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
        MethodCallByNameVisitor methodCallByNameVisitor = new MethodCallByNameVisitor();
        astRoot.accept(methodCallByNameVisitor, null);
        List<MethodCallExpr> assertAllMethodCallExpressions = new ArrayList<>();
        for (MethodCallExpr methodCallExpr : methodCallByNameVisitor.getMethodCalls()) {
            if (ASSERT_ALL_METHOD_NAME.equals(methodCallExpr.getNameAsString())) {
                assertAllMethodCallExpressions.add(methodCallExpr);
            }
        }
        return assertAllMethodCallExpressions;
    }
}
