package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TryStmtVisitor;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects usage of try-with-resources statements. For compilation of the source code to be successful, the resource
 * in a try-with-resources statement has to be an {@link AutoCloseable}, so this is not validated any further.<br/>
 * So, a perfume should be detected if a try-statement uses at least one resource.
 */
@Slf4j
@EqualsAndHashCode
public class TryWithResourcesDetector implements Detector<Perfume> {

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
        this.perfume = concreteDetectable;
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
        this.analysisContext = analysisContext;
    }

    /**
     * Detect the perfume in a given type.
     *
     * @param type The type to analyse.
     * @return A list of detections of the perfume.
     */
    private List<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TryStmtVisitor tryStmtVisitor = new TryStmtVisitor();
        type.accept(tryStmtVisitor, null);
        List<TryStmt> tryStmts = tryStmtVisitor.getTryStmts();

        for (TryStmt tryStmt : tryStmts) {
            if (isPerfumed(tryStmt)) {
                detections.add(DetectedInstance.from(tryStmt, perfume, type));
            }
        }

        return detections;
    }

    /**
     * A try-statement is Perfumed if it has at least one resource.
     *
     * @param tryStmt The try-statement to check.
     * @return {@code true} if the try-statement uses at least one {@link AutoCloseable} resource.
     */
    private boolean isPerfumed(@NotNull TryStmt tryStmt) {
        List<Expression> resources = tryStmt.getResources();

        return !resources.isEmpty();
    }
}
