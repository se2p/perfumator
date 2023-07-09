package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.List;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;

/**
 * {@link Detector} for the 'Paired "equals" and "hashCode"' {@link Perfume}.
 */
@EqualsAndHashCode
public class EqualsAndHashCodePairDetector implements Detector<Perfume> {

    private static final String EQUALS_NAME = "equals";

    private static final String HASHCODE_NAME = "hashCode";

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
            if (isPerfumed(type)) {
                detections.add(DetectedInstance.from(type, perfume, type));
            }
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

    private boolean isPerfumed(TypeDeclaration<?> type) {
        ClassOrInterfaceDeclaration decl = as(type, ClassOrInterfaceDeclaration.class);
        if (decl != null && decl.isInterface()) {
            return false;
        }

        boolean overridesEquals = false;
        boolean overridesHashCode = false;

        for (MethodDeclaration method : type.getMethods()) {
            String methodName = method.getNameAsString();
            AccessSpecifier accessSpecifier = method.getAccessSpecifier();

            if (!AccessSpecifier.PUBLIC.equals(accessSpecifier)) {
                continue;
            }

            // Checking for the method's return type and Parameters is enough to validate the override
            if (EQUALS_NAME.equals(methodName)) {
                boolean returnsBoolean = method.getType().asString().equals("boolean");
                boolean hasSingleObjectParameter = method.getParameters()
                        .stream().filter(param -> param.getType().asString().equals("Object"))
                        .count() == 1;

                if (returnsBoolean && hasSingleObjectParameter) {
                    overridesEquals = true;
                }

            } else if (HASHCODE_NAME.equals(methodName)) {
                boolean returnsInt = method.getType().asString().equals("int");
                boolean hasNoParameters = method.getParameters().isEmpty();

                if (returnsInt && hasNoParameters) {
                    overridesHashCode = true;
                }
            }
        }

        return overridesEquals && overridesHashCode;
    }

}
