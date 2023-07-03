package de.jsilbereisen.perfumator.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;

import java.nio.file.Path;

/**
 * {@link Detectable} instance that was detected in a source file.
 * Holds meta information on the detection location etc.
 */
@Data
@Accessors(chain = true)
public class DetectedInstance<T extends Detectable> implements Comparable<DetectedInstance<T>> {

    @JsonIncludeProperties({"name"})
    @JsonUnwrapped(prefix = "detectable_")
    private T detectable;

    /**
     * Type name (e.g. class/interface name) where the {@link #detectable} was detected.
     */
    private String typeName;

    private int beginningLineNumber;

    private int endingLineNumber;

    @ToString.Exclude
    private String concreteCode;

    private Path sourceFile;

    public DetectedInstance() {
    }

    public DetectedInstance(@Nullable T detectable, @Nullable String typeName, int beginningLineNumber,
                            int endingLineNumber, @Nullable String concreteCode, @NotNull Path sourceFile) {
        this.detectable = detectable;
        this.typeName = typeName;
        this.beginningLineNumber = beginningLineNumber;
        this.endingLineNumber = endingLineNumber;
        this.concreteCode = concreteCode;
        this.sourceFile = sourceFile;
    }

    /**
     * Copy constructor.
     *
     * @param detectedInstance The {@link DetectedInstance} to be copied.
     */
    @SuppressWarnings("unchecked")
    public DetectedInstance(@NotNull DetectedInstance<T> detectedInstance) {
        this.detectable = detectedInstance.detectable != null ? (T) detectedInstance.detectable.clone() : null;
        this.typeName = detectedInstance.typeName;
        this.beginningLineNumber = detectedInstance.beginningLineNumber;
        this.endingLineNumber = detectedInstance.endingLineNumber;
        this.concreteCode = detectedInstance.concreteCode;
        this.sourceFile = detectedInstance.sourceFile;
    }

    // TODO: i18n? Wenn dann ist Detectable bereits i18n
    // TODO: toString

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node            {@link NodeWithRange} to extract positional information.
     * @param detected        {@link T} that was detected by a {@link Detector <T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param compilationUnit AST as a {@link CompilationUnit} in which the {@link T} was detected.
     * @param <T>             The concrete Subtype of {@link Detectable}.
     * @return A {@link DetectedInstance<T>} with the given information.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(@NotNull NodeWithRange<?> node, @NotNull T detected,
                                                                  @NotNull CompilationUnit compilationUnit) {
        DetectedInstance<T> detectedInstance = from(node, detected);
        compilationUnit.getPrimaryTypeName().ifPresent(detectedInstance::setTypeName);

        return detectedInstance;
    }

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node            {@link NodeWithRange} to extract positional information.
     * @param detected        {@link T} that was detected by a {@link Detector <T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param typeDeclaration {@link TypeDeclaration} of the type in which the {@link T} was detected.
     * @param <T>             The concrete Subtype of {@link Detectable}.
     * @return A {@link DetectedInstance<T>} with the given information.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(@NotNull NodeWithRange<?> node, @NotNull T detected,
                                                                  @NotNull TypeDeclaration<?> typeDeclaration) {
        DetectedInstance<T> detectedInstance = from(node, detected);
        detectedInstance.setTypeName(typeDeclaration.getName().getIdentifier());

        return detectedInstance;
    }

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node           {@link NodeWithRange} to extract positional information.
     * @param detected       {@link T} that was detected by a {@link Detector<T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param parentTypeName Name of the type (e.g. Class name) in which the {@link T} was detected.
     * @param <T>            The concrete Subtype of {@link Detectable}.
     * @return A {@link DetectedInstance<T>} with the given information.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(@NotNull NodeWithRange<?> node, @Nullable T detected,
                                                                  @Nullable String parentTypeName) {
        return from(node, detected).setTypeName(parentTypeName);
    }

    /**
     * Returns a {@link DetectedInstance<T>}, filled with information from the given
     * parameters (if the information is present).
     *
     * @param node     {@link NodeWithRange} to extract positional information.
     * @param detected {@link T} that was detected by a {@link Detector<T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param <T>      The concrete Subtype of {@link Detectable}.
     * @return A {@link DetectedInstance<T>} with the given information.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(@NotNull NodeWithRange<?> node, @Nullable T detected) {
        DetectedInstance<T> detectedInstance = new DetectedInstance<>();

        detectedInstance.setDetectable(detected);
        node.getBegin().ifPresent(pos -> detectedInstance.setBeginningLineNumber(pos.line));
        node.getEnd().ifPresent(pos -> detectedInstance.setEndingLineNumber(pos.line));
        detectedInstance.setConcreteCode(node.toString());

        return detectedInstance;
    }

    /**
     * Compares two {@link DetectedInstance}s. The following (logical) order of comparison is applied:
     * <ul>
     *     <li>Compare the source-file-paths of the detections lexicographically. If only one of the paths is {@code null},
     *     that instance is seen as "greater", to appear at the end of lists.</li>
     *     <li>Compare the via their (override of the) {@link Detectable#compareTo} implementation.</li>
     *     <li>Compare by type name lexicographically (=&gt; types are within the same source file).</li>
     *     <li>Compare by starting line- and then starting column-number of the detection.
     *     The detection that happened at a smaller line number is seen as "less", to appear earlier in listings.</li>
     * </ul>
     *
     * @param other the object to be compared.
     * @return The comparison result after applying the above described criteria.
     */
    @Override
    public int compareTo(@NotNull DetectedInstance<T> other) {
        // first compare by source file where it was detected
        int sourceFileComparison = 0;
        if (sourceFile != null) {
            if (other.sourceFile != null) {
                sourceFileComparison = sourceFile.compareTo(other.sourceFile);
            } else {
                sourceFileComparison = 1;
            }
        } else {
            sourceFileComparison = other.sourceFile == null ? 0 : -1;
        }
        if (sourceFileComparison != 0) {
            return sourceFileComparison;
        }

        // compare by Detectable
        int detectableComparisonResult = 0;
        if (detectable != null) {
            detectableComparisonResult = detectable.compareTo(other.detectable);
        } else {
            detectableComparisonResult = other.detectable == null ? 0 : -1;
        }
        if (detectableComparisonResult != 0) {
            return detectableComparisonResult;
        }

        // compare by type name
        int typeNameComparisonResult = 0;
        if (typeName != null) {
            typeNameComparisonResult = typeName.compareTo(other.getTypeName());
        } else {
            typeNameComparisonResult = other.getTypeName() == null ? 0 : -1;
        }
        if (typeNameComparisonResult != 0) {
            return typeNameComparisonResult;
        }

        // compare by begin line numbers
        int beginLineNumberComparisonResult = beginningLineNumber - other.getBeginningLineNumber();
        if (beginLineNumberComparisonResult != 0) {
            return beginLineNumberComparisonResult;
        }

        // final comparison on ending line numbers
        return endingLineNumber - other.endingLineNumber;
    }
}
