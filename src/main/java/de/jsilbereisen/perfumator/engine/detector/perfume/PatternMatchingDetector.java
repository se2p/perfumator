package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternMatchingDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

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

    @NotNull
    @SuppressWarnings("unchecked")
    private List<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        List<InstanceOfExpr> instanceOfsWithPattern = type.findAll(InstanceOfExpr.class,
                expr -> expr.getPattern().isPresent());
        instanceOfsWithPattern.removeIf(instanceOfWithPattern -> instanceOfWithPattern.findAncestor(TypeDeclaration.class)
                .map(typeDecl -> !typeDecl.getNameAsString().equals(type.getNameAsString()))
                .orElse(true));

        if (instanceOfsWithPattern.isEmpty()) {
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();
        instanceOfsWithPattern.forEach(instanceOfWithPattern ->
                detections.add(DetectedInstance.from(instanceOfWithPattern, perfume, type)));

        return detections;
    }
}
