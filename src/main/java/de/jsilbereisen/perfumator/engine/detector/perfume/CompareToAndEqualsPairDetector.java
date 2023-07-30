package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.util.CompareToMethodDeclarationMatcher;
import de.jsilbereisen.perfumator.engine.detector.util.EqualsMethodDeclarationMatcher;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.CodeRange;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.jsilbereisen.perfumator.util.NodeUtil.findFirstMatch;
import static de.jsilbereisen.perfumator.util.NodeUtil.resolveFromStandardLibrary;
import static de.jsilbereisen.perfumator.util.NodeUtil.resolveSafely;
import static de.jsilbereisen.perfumator.util.NodeUtil.safeCheckAssignableBy;

/**
 * {@link Detector} for the "Override 'compareTo' with 'equals'" {@link Perfume}.
 * The contract of {@link Comparable#compareTo} recommends that two objects should be seen as equal be their {@code
 * compareTo} implementation when they are seen as equal by their {@code equals} implementation, and vice versa.
 * So, this detector detects pairings of overrides of {@code equals} and {@code compareTo}, and checks whether the
 * type is actually a {@link Comparable}.
 */
public class CompareToAndEqualsPairDetector implements Detector<Perfume> {

    /**
     * Fully qualified name of the {@link Comparable} interface from the Java standard library.
     */
    public static final String COMPARABLE = "java.lang.Comparable";

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        if (analysisContext == null) {
            return Collections.emptyList(); // Need resolution context to resolve the Comparable interface
        }

        ResolvedReferenceTypeDeclaration comparable = resolveFromStandardLibrary(COMPARABLE, analysisContext, "Unable"
                + " to resolve " + COMPARABLE + " from the standard library.");

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<TypeDeclaration<?>> types = new ArrayList<>();
        // Exclude Enums (and Annotations) because "equals" cant be overridden by enums
        types.addAll(typeVisitor.getClassOrInterfaceDeclarations());
        types.addAll(typeVisitor.getRecordDeclarations());

        for (TypeDeclaration<?> type : types) {
            analyseType(type, comparable).ifPresent(detections::add);
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
     * First checks if the given type overrides {@code equals} and {@code compareTo}. If so, tries to resolve the
     * type and verifies, that the given type is assignable to {@link Comparable}.<br/>
     * Uses symbol resolution.
     *
     * @param type The type to analyse. Should only be {@link ClassOrInterfaceDeclaration} or
     *             {@link RecordDeclaration}, otherwise does not make sense to analyse it.
     * @param comparableDeclaration Resolved declaration of {@link Comparable}.
     * @return If the above described conditions are met, returns an {@link Optional} of a {@link DetectedInstance}
     * for the {@link Perfume}, with the {@link CodeRange}s of the detected {@code equals} and {@code compareTo}
     * overrides. Otherwise, returns {@link Optional#empty()}.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    private Optional<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type,
                                                            @NotNull ResolvedReferenceTypeDeclaration comparableDeclaration) {
        if (!type.isClassOrInterfaceDeclaration() && !type.isRecordDeclaration()) {
            return Optional.empty();
        }

        Optional<MethodDeclaration> equalsOverride = findFirstMatch(type, new EqualsMethodDeclarationMatcher());
        Optional<MethodDeclaration> compareToOverride = findFirstMatch(type, new CompareToMethodDeclarationMatcher());
        if (equalsOverride.isEmpty() || compareToOverride.isEmpty()) {
            return Optional.empty();
        }

        Optional<ResolvedReferenceTypeDeclaration> resolvedTypeDecl = resolveSafely(
                (Resolvable<ResolvedReferenceTypeDeclaration>) type, this, type.getNameAsString());
        if (resolvedTypeDecl.isEmpty()) {
            return Optional.empty();
        }

        return safeCheckAssignableBy(comparableDeclaration, resolvedTypeDecl.get())
                ? Optional.of(DetectedInstance.from(perfume, type, equalsOverride.get(), compareToOverride.get()))
                : Optional.empty();
    }
}
