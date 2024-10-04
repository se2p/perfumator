package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodCallByNameVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EqualsAndHashCode
public class ThreadSafeSwingDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    private final static String SWING_UTILITIES = "SwingUtilities";
    private final static String INVOKE_LATER = "invokeLater";
    private final static String INVOKE_AND_WAIT = "invokeAndWait";
    private final static Set<String> RELEVANT_METHOD_NAMES = Set.of(INVOKE_LATER, INVOKE_AND_WAIT);
    private final static Map<String, String> RELEVANT_IMPORTS = 
            Map.of(INVOKE_LATER, "javax.swing.SwingUtilities.invokeLater", 
                   INVOKE_AND_WAIT, "javax.swing.SwingUtilities.invokeAndWait");
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        Set<String> staticImports = getStaticImports(astRoot);
        List<MethodCallExpr> methodCalls = getInvokeLaterInvokeAndWaitMethodCalls(astRoot, staticImports);
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
    
    private Set<String> getStaticImports(@NotNull CompilationUnit astRoot) {
        Set<String> importedAnnotations = new HashSet<>();

        for (ImportDeclaration importDeclaration : astRoot.getImports()) {
            for (Map.Entry<String, String> annotationToImport : RELEVANT_IMPORTS.entrySet()) {
                if (importDeclaration.getNameAsString().equals(annotationToImport.getValue())) {
                    importedAnnotations.add(annotationToImport.getKey());
                }
            }
        }
        return importedAnnotations;
    }
    
    private List<MethodCallExpr> getInvokeLaterInvokeAndWaitMethodCalls(@NotNull CompilationUnit astRoot, 
                                                                        Set<String> imports) {
        MethodCallByNameVisitor methodCallByNameVisitor = new MethodCallByNameVisitor();
        astRoot.accept(methodCallByNameVisitor, null);
        List<MethodCallExpr> relevantMethodCallExpressions = new ArrayList<>();
        for (MethodCallExpr methodCallExpr : methodCallByNameVisitor.getMethodCalls()) {
            if (RELEVANT_METHOD_NAMES.contains(methodCallExpr.getNameAsString())) {
                if (isPartOfSwingUtilities(methodCallExpr)) {
                    relevantMethodCallExpressions.add(methodCallExpr);
                } else if (imports.contains(methodCallExpr.getNameAsString())) {
                    relevantMethodCallExpressions.add(methodCallExpr);
                }
            }
        }
        return relevantMethodCallExpressions;
    }
    
    private boolean isPartOfSwingUtilities(MethodCallExpr methodCallExpr) {
        var scope = methodCallExpr.getScope();
        return scope.map(expression -> expression.toString().equals(SWING_UTILITIES)).orElse(false);
    }
}
