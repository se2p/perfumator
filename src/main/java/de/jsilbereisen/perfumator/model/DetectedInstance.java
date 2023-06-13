package de.jsilbereisen.perfumator.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Detectable} instance that was detected in a source file.
 * Holds meta information on the detection location etc.
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class DetectedInstance<T extends Detectable> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "detectableClass")
    private T detectable;

    private String parentTypeName;

    private int beginningLineNumber;

    private int endingLineNumber;

    private String concreteCode;

    public DetectedInstance() { }

    public DetectedInstance(@Nullable T detectable, @Nullable String parentTypeName, int beginningLineNumber,
                            int endingLineNumber, @Nullable String concreteCode) {
        this.detectable = detectable;
        this.parentTypeName = parentTypeName;
        this.beginningLineNumber = beginningLineNumber;
        this.endingLineNumber = endingLineNumber;
        this.concreteCode = concreteCode;
    }

    // TODO: i18n
    // TODO: toString

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node {@link NodeWithRange} to extract positional information.
     * @param detected {@link T} that was detected by a {@link Detector <T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param compilationUnit AST as a {@link CompilationUnit} in which the {@link T} was detected.
     * @return A {@link DetectedInstance<T>} with the given information.
     * @param <T> The concrete Subtype of {@link Detectable}.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(NodeWithRange<?> node, T detected,
                                                                  CompilationUnit compilationUnit) {
        DetectedInstance<T> detectedInstance = from(node, detected);
        compilationUnit.getPrimaryTypeName().ifPresent(detectedInstance::setParentTypeName);

        return detectedInstance;
    }

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node {@link NodeWithRange} to extract positional information.
     * @param detected {@link T} that was detected by a {@link Detector<T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param parentTypeName Name of the type (e.g. Class name) in which the {@link T} was detected.
     * @return A {@link DetectedInstance<T>} with the given information.
     * @param <T> The concrete Subtype of {@link Detectable}.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(NodeWithRange<?> node, T detected,
                                                                  String parentTypeName) {
        return from(node, detected).setParentTypeName(parentTypeName);
    }

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node {@link NodeWithRange} to extract positional information.
     * @param detected {@link T} that was detected by a {@link Detector<T>} and should be linked to the {@link DetectedInstance<T>}.
     * @return A {@link DetectedInstance<T>} with the given information.
     * @param <T> The concrete Subtype of {@link Detectable}.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(NodeWithRange<?> node, T detected) {
        DetectedInstance<T> detectedInstance = new DetectedInstance<>();

        detectedInstance.setDetectable(detected);
        node.getBegin().ifPresent(pos -> detectedInstance.setBeginningLineNumber(pos.line));
        node.getEnd().ifPresent(pos -> detectedInstance.setEndingLineNumber(pos.line));
        detectedInstance.setConcreteCode(node.toString());

        return detectedInstance;
    }
}
