package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.utils.Pair;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodCallByNameVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;
import static de.jsilbereisen.perfumator.util.NodeUtil.resolveSafely;

/**
 * {@link Detector} for the "Use optimized collections for Enums" {@link Perfume}.
 * The Perfume rewards usage of the optimized {@link Collection} implementations for enums, namely {@link EnumSet}
 * and {@link EnumMap}. For {@link EnumSet}, we check for calls to its static factory methods (see {@link #ENUM_SET_FACTORY_METHODS}
 * for the recognized ones) and for {@link EnumMap}, we check for a call to one of its constructors.
 */
@EqualsAndHashCode
public class OptimizedEnumCollectionsDetector implements Detector<Perfume> {

    public static final String JAVA_UTIL_PACKAGE = "java.util";

    public static final Pair<String, String> ENUM_SET_CLASS = new Pair<>("EnumSet", JAVA_UTIL_PACKAGE + ".EnumSet");

    public static final Pair<String, String> ENUM_MAP_CLASS = new Pair<>("EnumMap", JAVA_UTIL_PACKAGE + ".EnumMap");

    public static final Set<String> ENUM_SET_FACTORY_METHODS = Set.of("of", "allOf", "noneOf", "range", "copyOf", "complementOf");

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        // Optimization: only one AST traversal, new visitor that registers all method-calls + "new ..." expressions
        detections.addAll(detectEnumSetMethodCalls(astRoot));
        detections.addAll(detectEnumMapConstructorCalls(astRoot));

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

    private List<DetectedInstance<Perfume>> detectEnumSetMethodCalls(@NotNull CompilationUnit ast) {
        MethodCallByNameVisitor visitor = new MethodCallByNameVisitor();
        ast.accept(visitor, ENUM_SET_FACTORY_METHODS);
        List<MethodCallExpr> potentialEnumSetCalls = visitor.getMethodCalls();

        if (potentialEnumSetCalls.isEmpty()) {
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();
        for (MethodCallExpr methodCall : potentialEnumSetCalls) {
            if (isPerfumedFactoryMethod(methodCall, ast)) {
                detections.add(DetectedInstance.from(perfume, methodCall, ast));
            }
        }

        return detections;
    }

    private boolean isPerfumedFactoryMethod(@NotNull MethodCallExpr methodCall, @NotNull CompilationUnit ast) {
        // Resolve method call
        Optional<ResolvedMethodDeclaration> resolved = resolveSafely(methodCall, this, methodCall.getNameAsString());
        if (resolved.isPresent()) {
            ResolvedMethodDeclaration decl = resolved.get();
            return decl.declaringType().getQualifiedName().equals(ENUM_SET_CLASS.b);
        }

        // if resolution not possible (e.g. if it can't resolve the given argument),
        // we go the "unsafe" route of checking the scope expr + imports ourselves
        Optional<Expression> scope = methodCall.getScope();
        if (scope.isEmpty()) {
            return ast.getImports().stream().filter(ImportDeclaration::isStatic)
                    .anyMatch(importDecl -> (importDecl.isAsterisk() && importDecl.getNameAsString().equals(ENUM_SET_CLASS.b))
                            || (!importDecl.isAsterisk() && importDecl.getNameAsString().equals(ENUM_SET_CLASS.b + "." + methodCall.getNameAsString())));
        }

        NameExpr scopeName = as(scope.get(), NameExpr.class);
        if (scopeName == null || !scopeName.getNameAsString().equals(ENUM_SET_CLASS.a)) {
            return false;
        }

        return ast.getImports().stream().filter(importDecl -> !importDecl.isStatic())
                .anyMatch(importDecl -> importDecl.getNameAsString().equals(ENUM_SET_CLASS.b)
                        || (importDecl.isAsterisk() && importDecl.getNameAsString().equals(JAVA_UTIL_PACKAGE)));
    }

    private List<DetectedInstance<Perfume>> detectEnumMapConstructorCalls(@NotNull CompilationUnit ast) {
        List<ObjectCreationExpr> enumMapConstructorCalls = ast.findAll(ObjectCreationExpr.class,
                expr -> expr.getType().getNameAsString().equals(ENUM_MAP_CLASS.a));

        if (enumMapConstructorCalls.isEmpty()) {
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        // Verify by resolving the type - should never fail, as long as reflection-resolution is configured (and the
        // source file has the necessary import for the class)
        for (ObjectCreationExpr expr : enumMapConstructorCalls) {
            Optional<ResolvedType> resolved = resolveSafely(expr.getType(), this, expr.getTypeAsString());

            if (resolved.isEmpty() || !resolved.get().isReferenceType()) {
                continue;
            }

            if (resolved.get().asReferenceType().getQualifiedName().equals(ENUM_MAP_CLASS.b)) {
                detections.add(DetectedInstance.from(perfume, expr, ast));
            }
        }

        return detections;
    }
}
