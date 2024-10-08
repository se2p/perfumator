package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.SwitchStmtVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Detector for the "Defensive Default case" {@link Perfume} pattern.
 * Detects usage of the "default" case in a switch statement.<br/>
 * <b>Problem</b> with JavaParser: when there is only a comment in the default-case in the source
 * file, the comment is missing in the AST.
 */
@EqualsAndHashCode
public class DefensiveDefaultCaseDetector implements Detector<Perfume> {

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();

        SwitchStmtVisitor switchVisitor = new SwitchStmtVisitor();
        astRoot.accept(switchVisitor, null);
        List<SwitchStmt> switchStmts = switchVisitor.getSwitchStmts();

        for (SwitchStmt switchStmt : switchStmts) {
            Optional<SwitchEntry> defaultCase = switchStmt.getEntries().stream()
                    .filter(entry -> entry.getLabels().isEmpty()).findFirst();

            defaultCase.ifPresent(o -> detectedInstances.add(DetectedInstance.from(switchStmt, perfume, astRoot)));
        }

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
}
