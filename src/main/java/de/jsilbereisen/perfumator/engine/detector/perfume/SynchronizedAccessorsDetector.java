package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;

/**
 * {@link Detector} for the "Synchronize accessors in pairs" {@link Perfume}.
 * Detects groups of accessors (overloads are considered), and checks whether all accessors in a group
 * fulfill these criteria:
 * <ul>
 *     <li>The accessor is synchronized by using the {@code synchronized} keyword in the method declaration</li>
 *     <li>The first statement of the accessor is a {@link SynchronizedStmt}, synchronizing on {@code this} or a
 *     class (for static fields)</li>
 * </ul>
 * Accessors are here identified through their usual naming, which leads to missing of accessors if these do not follow
 * the "get"/"set" naming strategy, but not following this naming should probably not be worth of being perfumed anyway.
 */
// TODO: "is" fuer getter von boolean-Variable bedenken
@EqualsAndHashCode
public class SynchronizedAccessorsDetector implements Detector<Perfume> {

    /**
     * We identify accessors by their usual naming. Even though this might miss accessors, when an
     * accessor doesn't follow the naming, it is probably not worth being perfumed.
     */
    public static final Pattern ACCESSOR_PATTERN = Pattern.compile("^(get|set)[A-Z]\\w*$");

    public static final Pattern GETTER = Pattern.compile("^get[A-Z]\\w*$");

    public static final Pattern SETTER = Pattern.compile("^set[A-Z]\\w*$");

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<TypeDeclaration<?>> types = typeVisitor.getAllTypeDeclarations();

        for (TypeDeclaration<?> type : types) {
            detections.addAll(analyseAccessors(type));
        }

        return detections;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        this.perfume = concreteDetectable;
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
        this.analysisContext = analysisContext;
    }

    @NotNull
    private List<DetectedInstance<Perfume>> analyseAccessors(@NotNull TypeDeclaration<?> type) {
        Map<String, List<MethodDeclaration>> accessorGroupsMap = new HashMap<>();

        for (MethodDeclaration method : type.getMethods()) {
            if (!ACCESSOR_PATTERN.matcher(method.getNameAsString()).matches()) {
                continue;
            }

            accessorGroupsMap.computeIfAbsent(method.getNameAsString().substring(3), k -> new ArrayList<>())
                    .add(method);
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        for (List<MethodDeclaration> group : accessorGroupsMap.values()) {
            boolean hasGetter = false;
            boolean hasSetter = false;

            for (MethodDeclaration method : group) {
                String methodName = method.getNameAsString();

                if (GETTER.matcher(method.getNameAsString()).matches()) {
                    hasGetter = true;
                } else if (SETTER.matcher(methodName).matches()) {
                    hasSetter = true;
                }

                if (hasGetter && hasSetter) {
                    if (isPerfumed(group)) {
                        detections.add(DetectedInstance.from(perfume, type, group.toArray(new MethodDeclaration[] {})));
                    }
                    break;
                }
            }
        }

        return detections;
    }

    private boolean isPerfumed(@NotNull List<MethodDeclaration> accessorsGroup) {
        assert accessorsGroup.size() >= 2 : "Accessor group must consist of at least a getter and setter, but had " +
                "size " + accessorsGroup.size();

        return accessorsGroup.stream().allMatch(this::isAccessorSynchronized);
    }

    private boolean isAccessorSynchronized(@NotNull MethodDeclaration accessor) {
        if (accessor.isSynchronized()) {
            return true;
        }

        Optional<BlockStmt> body = accessor.getBody();
        if (body.isEmpty()) {
            return false;
        }

        Optional<Statement> first = body.get().getStatements().getFirst();
        if (first.isEmpty()) {
            return false;
        }

        SynchronizedStmt synchronizedStmt = as(first.get(), SynchronizedStmt.class);
        if (synchronizedStmt == null) {
            return false;
        }

        return synchronizedStmt.getExpression().isThisExpr() || synchronizedStmt.getExpression().isClassExpr();
    }
}
