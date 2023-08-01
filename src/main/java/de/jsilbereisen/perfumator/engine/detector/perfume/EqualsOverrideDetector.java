package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.util.EqualsMethodDeclarationMatcher;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.jsilbereisen.perfumator.util.NodeUtil.findFirstMatch;
import static de.jsilbereisen.perfumator.util.NodeUtil.resolveSafely;

/**
 * {@link Detector} for the "Override equals of superclass" {@link Perfume}. This detector heavily relies on
 * dependencies of the source code (in AST form), that is to be analysed, to be provided, in order to be able to
 * resolve inheritance hierarchies.
 */
@Slf4j
@EqualsAndHashCode
public class EqualsOverrideDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<ClassOrInterfaceDeclaration> classes = typeVisitor.getClassOrInterfaceDeclarations();

        for (ClassOrInterfaceDeclaration clazz : classes) {
            analyseClass(clazz).ifPresent(detections::add);
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
    private Optional<DetectedInstance<Perfume>> analyseClass(@NotNull ClassOrInterfaceDeclaration clazz) {
        if (clazz.isInterface()) {
            return Optional.empty();
        }

        // First check if this class does have any non-static fields
        if (clazz.getFields().stream().filter(field -> !field.isStatic()).findFirst().isEmpty()) {
            return Optional.empty();
        }

        // Check if it overrides equals
        Optional<MethodDeclaration> equalsOverride = findFirstMatch(clazz, new EqualsMethodDeclarationMatcher());
        if (equalsOverride.isEmpty()) {
            return Optional.empty();
        }

        // Only then check if any superclass overrides equals (requeries transitive resolution of ancestors)
        if (!hasSuperclassThatOverridesEquals(clazz)) {
            return Optional.empty();
        }

        return Optional.of(DetectedInstance.from(equalsOverride.get(), perfume, clazz));
    }

    private boolean hasSuperclassThatOverridesEquals(@NotNull ClassOrInterfaceDeclaration clazz) {
        if (clazz.isInterface()) {
            return false;
        }

        if (clazz.getExtendedTypes().isEmpty()) {
            return false;
        }

        // Resolve the given class declaration
        Optional<ResolvedReferenceTypeDeclaration> resolvedClass = resolveSafely(clazz, this, clazz.getNameAsString());
        if (resolvedClass.isEmpty()) {
            return false; // Resolution fails => Can't know if perfumed
        }

        // Resolve ALL ancestors (includes transitive ones)
        List<ResolvedReferenceType> resolvedAncestors;
        try {
            resolvedAncestors = resolvedClass.get().getAllAncestors(ResolvedReferenceTypeDeclaration.breadthFirstFunc);
        } catch (UnsolvedSymbolException e) {
            log.debug("Could not resolve all ancestors for resolved reference type \"{}\".", resolvedClass.get().getQualifiedName());
            log.debug("Caused by exception: ", e);
            return false;
        }

        // Filter out all interfaces + java.lang.Object (and those who have not resolved declaration)
        resolvedAncestors.removeIf(ancestor -> {
            if (ancestor.isJavaLangObject()) {
                return true;
            }

            return ancestor.getTypeDeclaration().map(ResolvedTypeDeclaration::isInterface).orElse(true);
        });

        return resolvedAncestors.stream().anyMatch(this::overridesEquals);
    }

    private boolean overridesEquals(@NotNull ResolvedReferenceType resolvedReferenceType) {
        Set<MethodUsage> declaredMethods = resolvedReferenceType.getDeclaredMethods();

        for (MethodUsage  method : declaredMethods) {
            ResolvedMethodDeclaration declaration = method.getDeclaration();

            if (!declaration.accessSpecifier().equals(AccessSpecifier.PUBLIC)
                    || !declaration.getName().equals("equals")
                    || !declaration.getReturnType().equals(ResolvedPrimitiveType.BOOLEAN)
                    || declaration.getNumberOfParams() != 1) {
                continue;
            }

            ResolvedParameterDeclaration resolvedParam = declaration.getParam(0);

            if (resolvedParam.getType().isReferenceType()) {
                ResolvedReferenceType refType = resolvedParam.getType().asReferenceType();

                if (refType.isJavaLangObject()) {
                    return true;
                }
            }
        }

        return false;
    }
}
