package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;

// TODO: Doc
@EqualsAndHashCode
public class PackagePrivateTestsDetector implements Detector<Perfume> {

    /**
     * Mapping of JUnit 5 test-annotations to their fully qualified name.
     */
    public static final Map<String, String> TEST_ANNOTATIONS = Map.of(
            "Test", "org.junit.jupiter.api.Test",
            "ParameterizedTest", "org.junit.jupiter.params.ParameterizedTest"
    );

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        Set<String> importedAnnotations = analyseImports(astRoot);
        if (importedAnnotations.isEmpty()) {
            return Collections.emptyList();
        }

        // We only look at the primary class of the AST here
        Optional<ClassOrInterfaceDeclaration> primaryType = astRoot.getPrimaryType()
                .map(type -> as(type, ClassOrInterfaceDeclaration.class));
        if (primaryType.isEmpty()) {
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();
        analyseTestClass(primaryType.get(), importedAnnotations).ifPresent(detections::add);

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
     * Returns a subset of the keys of {@link #TEST_ANNOTATIONS}, consisting of those annotation names that are
     * imported.
     *
     * @param astRoot The AST with the imports to analyse.
     * @return The set of names of the imported interesting annotations.
     */
    @NotNull
    private Set<String> analyseImports(@NotNull CompilationUnit astRoot) {
        Set<String> importedAnnotations = new HashSet<>();

        for (ImportDeclaration importDeclaration : astRoot.getImports()) {
            for (Map.Entry<String, String> annotationToImport : TEST_ANNOTATIONS.entrySet()) {
                if (importDeclaration.getNameAsString().equals(annotationToImport.getValue())) {
                    importedAnnotations.add(annotationToImport.getKey());
                }
            }
        }

        return importedAnnotations;
    }

    /**
     * Analyses a class and the declared tests. Returns a {@link DetectedInstance} of the {@link Perfume} for
     * the class, if all the following criteria are met:
     * <ul>
     *     <li>The class is declared package-private and is not an interface nor abstract.</li>
     *     <li>Every test method is declared package-private.</li>
     *     <li>The class has at least one test.</li>
     * </ul>
     * @param classDeclaration The class to analyse.
     * @param importedAnnotations The names of the annotations that are imported and thus available.
     * @return An {@link Optional} with the {@link DetectedInstance} of the {@link Perfume} for the class, if the
     *         criteria described above are met. Otherwise, returns {@link Optional#empty()}.
     */
    private Optional<DetectedInstance<Perfume>> analyseTestClass(
            @NotNull ClassOrInterfaceDeclaration classDeclaration, @NotNull Set<String> importedAnnotations) {
        if (classDeclaration.isInterface() || classDeclaration.isAbstract() || importedAnnotations.isEmpty()) {
            return Optional.empty();
        }

        // First check whether the class is package-rpivate
        if (classDeclaration.isPrivate() || classDeclaration.isProtected() || classDeclaration.isPublic()) {
            return Optional.empty();
        }

        // Go over all methods. If it's a unit test, check whether its package-private. If not => Optional#empty
        boolean hasUnitTest = false;
        for (MethodDeclaration method : classDeclaration.getMethods()) {
            boolean hasTestAnnotation = importedAnnotations.stream()
                    .anyMatch(annotationName -> method.getAnnotationByName(annotationName).isPresent());

            if (!hasTestAnnotation) {
                continue;
            }

            hasUnitTest = true;

            if (method.isPrivate() || method.isProtected() || method.isPublic()) {
                return Optional.empty();
            }
        }

        // No unit test => no perfume
        if (!hasUnitTest) {
            return Optional.empty();
        }

        return Optional.of(DetectedInstance.from(classDeclaration, perfume, classDeclaration));
    }
}
