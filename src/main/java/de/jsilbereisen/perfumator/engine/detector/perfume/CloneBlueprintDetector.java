package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithImplements;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodCallByNameVisitor;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.jsilbereisen.perfumator.util.NodeUtil.resolveFromStandardLibrary;
import static de.jsilbereisen.perfumator.util.NodeUtil.resolveSafely;
import static de.jsilbereisen.perfumator.util.NodeUtil.safeCheckAssignableBy;

/**
 * {@link Detector} for the "Clone blueprint" {@link Perfume}. Verifies the following criteria, in order for a method
 * to be perfumed:<br/>
 * <ul>
 *     <li>The method overrides {@link Object#clone()} with visibility {@code public}.</li>
 *     <li>The method at some point calls {@code super.clone()}.</li>
 *     <li>The class that contains the method implements {@link Cloneable} (or inherits it from an ancestor).</li>
 * </ul>
 */
@EqualsAndHashCode
public class CloneBlueprintDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<TypeDeclaration<?>> types = new ArrayList<>();
        types.addAll(typeVisitor.getClassOrInterfaceDeclarations());
        types.addAll(typeVisitor.getRecordDeclarations());

        for (TypeDeclaration<?> type : types) {
            analyseType(type).ifPresent(det -> detections.add(det));
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
    private Optional<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        if (!implementsCloneable(type)) {
            return Optional.empty();
        }

        Optional<MethodDeclaration> cloneMethod = type.getMethodsByName("clone").stream()
                .filter(method -> method.isPublic() && method.getParameters().isEmpty()).findFirst();

        return cloneMethod.filter(this::callsSuperClone)
                .map(methodDeclaration -> DetectedInstance.from(methodDeclaration, perfume, type));
    }

    @SuppressWarnings("unchecked")
    private boolean implementsCloneable(@NotNull TypeDeclaration<?> type) {
        Optional<ResolvedReferenceTypeDeclaration> resolvedType = resolveSafely(
                (Resolvable<ResolvedReferenceTypeDeclaration>) type, this,
                type.getFullyQualifiedName().orElse(type.getNameAsString()));

        // The most uncertain way to verify whether we are a "cloneable", but the only way in this case.
        if (resolvedType.isEmpty() || analysisContext == null) {
            return ((NodeWithImplements<?>) type).getImplementedTypes().stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("Cloneable"));
        }

        ResolvedReferenceTypeDeclaration cloneable = resolveFromStandardLibrary("java.lang.Cloneable",
                analysisContext, "Unable to resolve \"java.lang.Cloneable\" from the standard library.");

        return safeCheckAssignableBy(cloneable, resolvedType.get());
    }

    private boolean callsSuperClone(@NotNull MethodDeclaration methodDeclaration) {
        // Find all calls to "clone" within the clone method
        MethodCallByNameVisitor methodCallVisitor = new MethodCallByNameVisitor();
        methodDeclaration.accept(methodCallVisitor, Set.of("clone"));
        List<MethodCallExpr> calls = methodCallVisitor.getMethodCalls();

        // One call must be enough - otherwise something is fishy
        if (calls.size() != 1) {
            return false;
        }

        // Check the scope => is it called on "super"?
        MethodCallExpr cloneCall = calls.get(0);
        return cloneCall.getScope().map(scopeExpr -> scopeExpr.isSuperExpr() && cloneCall.getArguments().isEmpty()).orElse(false);
    }
}
