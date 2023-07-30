package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.util.EqualsMethodDeclarationMatcher;
import de.jsilbereisen.perfumator.engine.detector.util.HashCodeMethodDeclarationMatcher;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;
import static de.jsilbereisen.perfumator.util.NodeUtil.findFirstMatch;

/**
 * {@link Detector} for the 'Paired "equals" and "hashCode"' {@link Perfume}.
 */
@EqualsAndHashCode
public class EqualsAndHashCodePairDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    /**
     * Detects the 'Paired "equals" and "hashCode"' {@link Perfume}. We only look at regular classes and
     * {@link Record}s, meaning interfaces, {@link Enum}s (because {@link Enum#equals} is final) and Annotation are
     * ignored in the analysis, because these do not make sense.
     *
     * @param astRoot The root node of the AST in which the {@link Perfume} should be searched for.
     * @return The list of detections (at most size N, where N is the number of interesting types in the AST).
     */
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<TypeDeclaration<?>> types = new ArrayList<>(typeVisitor.getClassOrInterfaceDeclarations());
        types.addAll(typeVisitor.getRecordDeclarations());

        for (TypeDeclaration<?> type : types) {
            analyseType(type).ifPresent(detections::add);
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

    private Optional<DetectedInstance<Perfume>> analyseType(TypeDeclaration<?> type) {
        ClassOrInterfaceDeclaration decl = as(type, ClassOrInterfaceDeclaration.class);
        if (decl != null && decl.isInterface()) {
            return Optional.empty();
        }

        Optional<MethodDeclaration> equals = findFirstMatch(type, new EqualsMethodDeclarationMatcher());
        Optional<MethodDeclaration> hashCode = findFirstMatch(type, new HashCodeMethodDeclarationMatcher());

        return equals.isPresent() && hashCode.isPresent()
                ? Optional.of(DetectedInstance.from(perfume, type, equals.get(), hashCode.get()))
                : Optional.empty();
    }
}
