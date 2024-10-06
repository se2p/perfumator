package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Detector} for the "Parameterized Test" {@link Perfume}.
 * Detects the perfume only if the annotation is part of JUnit 5 ({@link org.junit.jupiter.params.ParameterizedTest}).
 */
@EqualsAndHashCode
public class ParameterizedTestDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;
    
    private static final String PARAMETERIZED_TEST_IDENTIFIER = "org.junit.jupiter.params.ParameterizedTest";

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
        return astRoot.findAll(MethodDeclaration.class, methodDeclaration -> methodDeclaration.getAnnotations().stream()
                .map(AnnotationExpr::resolve)
                .map(ResolvedTypeDeclaration::getQualifiedName)
                .anyMatch(name -> name.equals(PARAMETERIZED_TEST_IDENTIFIER)));
    }
}
