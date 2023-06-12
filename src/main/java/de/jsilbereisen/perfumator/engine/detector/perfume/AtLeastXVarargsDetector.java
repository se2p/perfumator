package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodDeclarationVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.Perfume;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtLeastXVarargsDetector implements Detector<Perfume> {

    private Perfume perfume;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        astRoot.accept(methodDeclarationVisitor, null);
        List<MethodDeclaration> methodDeclarations = methodDeclarationVisitor.getMethodDeclarations();

        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            Optional<DetectedInstance<Perfume>> detected = checkForPerfume(methodDeclaration);
            detected.ifPresent(det -> {
                astRoot.getPrimaryTypeName().ifPresent(det::setParentTypeName);
                detectedPerfumes.add(det);
            });
        }

        return detectedPerfumes;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        perfume = concreteDetectable;
    }

    @NotNull
    private Optional<DetectedInstance<Perfume>> checkForPerfume(@NotNull MethodDeclaration methodDeclaration) {
        if (methodDeclaration.isAbstract() || methodDeclaration.getBody().isEmpty()) {
            return Optional.empty();
        }

        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        if (parameters == null || parameters.stream().noneMatch(Parameter::isVarArgs)) {
            return Optional.empty();
        }

        Parameter varargsParameter = parameters.stream().filter(Parameter::isVarArgs).findFirst().get();
        if (parameters.stream().anyMatch(parameter -> parameter.getType().equals(varargsParameter.getType()))) {
            DetectedInstance<Perfume> detected = new DetectedInstance<>();
            detected.setDetectable(perfume);
            methodDeclaration.getBegin().ifPresent(pos -> detected.setBeginningLineNumber(pos.line));
            methodDeclaration.getEnd().ifPresent(pos -> detected.setEndingLineNumber(pos.line));
            detected.setConcreteCode(methodDeclaration.toString());

            return Optional.of(detected);
        }

        return Optional.empty();
    }
}
