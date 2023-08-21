package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.jsilbereisen.perfumator.util.NodeUtil.hasNonPrivateConstructor;

/**
 * Detector for the 'No utility initialization' {@link Perfume}. Detects a Perfume when all of a classes' methods are
 * static and the class has only private constructors.
 */
@Slf4j
@EqualsAndHashCode
public class NoUtilityInstantiationDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);

        List<ClassOrInterfaceDeclaration> relevantTypes = typeVisitor.getClassOrInterfaceDeclarations();
        relevantTypes.removeIf(ClassOrInterfaceDeclaration::isInterface);

        relevantTypes.forEach(type -> {
            Optional<DetectedInstance<Perfume>> detected = analyseType(type);

            detected.map(detectedInstance -> detectedInstance.setTypeName(type.getName().getIdentifier()))
                    .ifPresent(detectedInstances::add);
        });

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

    private Optional<DetectedInstance<Perfume>> analyseType(ClassOrInterfaceDeclaration type) {
        if (hasNonPrivateConstructor(type)) {
            return Optional.empty();
        }

        if (areNotUtilityMethods(type)) {
            return Optional.empty();
        }

        return Optional.of(DetectedInstance.from(type, perfume));
    }

    private boolean areNotUtilityMethods(ClassOrInterfaceDeclaration type) {
        List<MethodDeclaration> methods = type.getMethods();

        if (methods.isEmpty()) {
            return true;
        }

        return methods.stream().anyMatch(method -> !method.isStatic());
    }
}
