package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodDeclarationVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
public class ParameterizedTestDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String PARAMETERIZED_TEST_IDENTIFIER = "ParameterizedTest";
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodDeclaration> parameterizedTestMethodDeclarations = getParameterizedTestMethodDeclarations(astRoot);
        parameterizedTestMethodDeclarations
                .forEach(declaration -> detectedInstances.add(DetectedInstance.from(declaration, perfume, astRoot)));
        return detectedInstances;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        perfume = concreteDetectable;
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
        this.analysisContext = analysisContext;
    }
    
    private List<MethodDeclaration> getParameterizedTestMethodDeclarations(@NotNull CompilationUnit astRoot) {
        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        astRoot.accept(methodDeclarationVisitor, null);
        List<MethodDeclaration> parameterizedTestMethodDeclarations = new ArrayList<>();
        for (MethodDeclaration declaration : methodDeclarationVisitor.getMethodDeclarations()) {
            boolean hasParameterizedTestAnnotation = declaration.getAnnotations()
                    .stream()
                    .map(AnnotationExpr::getNameAsString)
                    .anyMatch(id -> id.equals(PARAMETERIZED_TEST_IDENTIFIER));

            if (hasParameterizedTestAnnotation) {
                parameterizedTestMethodDeclarations.add(declaration);
            }
        }
        return parameterizedTestMethodDeclarations;
    }
}
