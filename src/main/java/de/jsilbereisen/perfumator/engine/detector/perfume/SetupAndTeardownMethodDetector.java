package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Detector} for the "Setup or teardown method" {@link Perfume}.
 * Detects the perfume only if the annotation is part of the JUnit 5 package {@link org.junit.jupiter.api}.
 */
public class SetupAndTeardownMethodDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    public static final String IMPORT_QUALIFIER = "org.junit.jupiter.api.";
    public static final Set<String> TEST_ANNOTATIONS = Set.of("BeforeAll", "BeforeEach", "AfterAll", "AfterEach");
    
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
    
    private Set<String> getQualifiedAnnotations() {
        return TEST_ANNOTATIONS.stream().map(annotation -> IMPORT_QUALIFIER + annotation).collect(Collectors.toSet());
    }

    private List<MethodDeclaration> getSetupAndTeardownMethodDeclarations(@NotNull CompilationUnit astRoot) {
        return astRoot.findAll(MethodDeclaration.class, methodDeclaration -> methodDeclaration.getAnnotations().stream()
                .map(AnnotationExpr::resolve)
                .map(ResolvedTypeDeclaration::getQualifiedName)
                .anyMatch(name -> getQualifiedAnnotations().contains(name)));
    }
}
