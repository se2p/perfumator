package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.NodeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    /**
     * Shorthand to return the fully qualified names of the annotations.
     * 
     * @return The Set containing {org.junit.jupiter.api.BeforeEach, org.junit.jupiter.api.BeforeAll, 
     *         org.junit.jupiter.api.AfterEach, org.junit.jupiter.api.AfterAll}.
     */
    private Set<String> getQualifiedAnnotations() {
        return TEST_ANNOTATIONS.stream().map(annotation -> IMPORT_QUALIFIER + annotation).collect(Collectors.toSet());
    }

    private List<MethodDeclaration> getSetupAndTeardownMethodDeclarations(@NotNull CompilationUnit astRoot) {
        return astRoot.findAll(MethodDeclaration.class, methodDeclaration -> methodDeclaration.getAnnotations().stream()
                // filter out annotations that do not contain any of the four relevant annotations
                .filter(annotation -> TEST_ANNOTATIONS.stream().anyMatch(testAnnotation -> annotation.getNameAsString().contains(testAnnotation)))
                // try to resolve the symbol in order to get the declaration
                .map(testAnnotation -> NodeUtil.resolveSafely(testAnnotation, this, testAnnotation.getNameAsString()))
                .filter(Optional::isPresent)
                .map(resolvedAnnotationDeclaration -> resolvedAnnotationDeclaration.get().getQualifiedName())
                .anyMatch(qualifiedName -> getQualifiedAnnotations().contains(qualifiedName)));
    }
}
