package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodDeclarationVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetupAndTeardownMethodDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    public static final List<String> TEST_ANNOTATIONS = List.of("BeforeAll", "BeforeEach", "AfterAll", "AfterEach");
    
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();
        List<MethodDeclaration> setupAndTeardownMethods = getSetupAndTeardownMethodDeclarations(astRoot);
        setupAndTeardownMethods
                .forEach(declaration -> detectedInstances.add(DetectedInstance.from(declaration, perfume, astRoot)));
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
    
    private List<MethodDeclaration> getSetupAndTeardownMethodDeclarations(@NotNull CompilationUnit astRoot) {
        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        astRoot.accept(methodDeclarationVisitor, null);
        List<MethodDeclaration> setupAndTeardownMethodDeclarations = new ArrayList<>();
        for (MethodDeclaration declaration : methodDeclarationVisitor.getMethodDeclarations()) {
            for (AnnotationExpr annotation : declaration.getAnnotations()) {
                if (TEST_ANNOTATIONS.contains(annotation.getNameAsString())) {
                    setupAndTeardownMethodDeclarations.add(declaration);
                }
            }
        }
        return setupAndTeardownMethodDeclarations;
    }
}
