package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Detector} for the "Assert in private method" {@link Perfume}.
 * Detects a method as perfumed, if all the following conditions apply:
 * <ul>
 *     <li>The method is private.</li>
 *     <li>At least one of the method's parameters is used with an {@code assert} expression, and
 *     if that {@code assert} expression has an error message.</li>
 * </ul>
 */
public class AssertPrivateMethodDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    /**
     * Detects the perfume. See the classes JavaDoc for information about the applied criteria.
     *
     * @param astRoot The root node of the AST in which the {@link Perfume} should be searched for.
     * @return The list of detected {@link Perfume} instances.
     */
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<TypeDeclaration<?>> types = typeVisitor.getAllTypeDeclarations();

        for (TypeDeclaration<?> type : types) {
            detections.addAll(analyseType(type));
        }

        return detections;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        perfume = concreteDetectable;
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
        this.analysisContext = analysisContext;
    }

    /**
     * Analyses all methods that are declared in the type for being perfumed.
     *
     * @param type The {@link TypeDeclaration} to analyse.
     * @return The list of detections within the type.
     */
    @NotNull
    private List<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        List<MethodDeclaration> methodsToCheck = type.getMethods().stream()
                .filter(method -> method.isPrivate() && method.getParameters().isNonEmpty())
                .collect(Collectors.toList());

        if (methodsToCheck.isEmpty()) {
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        for (MethodDeclaration method : methodsToCheck) {
            if (isPerfumed(method)) {
                detections.add(DetectedInstance.from(method, perfume, type));
            }
        }

        return detections;
    }

    /**
     * Checks whether the method is perfumed. For the applied criteria, see the classes JavaDoc.
     *
     * @param method The method to check.
     * @return {@code true} if the method is perfumed.
     */
    private boolean isPerfumed(@NotNull MethodDeclaration method) {
        List<AssertStmt> assertsWithMsg = method.findAll(AssertStmt.class, assertStmt -> assertStmt.getMessage().isPresent());
        if (assertsWithMsg.isEmpty()) {
            return false;
        }

        Set<String> paramNames = method.getParameters().stream().map(Parameter::getNameAsString).collect(Collectors.toSet());
        if (paramNames.isEmpty()) {
            return false;
        }

        return assertsWithMsg.stream().anyMatch(assertStmt ->
                !assertStmt.findAll(NameExpr.class, name -> paramNames.contains(name.getNameAsString())).isEmpty());
    }
}
