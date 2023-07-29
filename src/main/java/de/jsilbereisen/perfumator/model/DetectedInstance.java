package de.jsilbereisen.perfumator.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.javaparser.HasParentNode;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;

import java.nio.file.Path;
import java.util.*;

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

    private final Set<CodeRange> codeRanges = new TreeSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final List<String> codeSnippets = new ArrayList<>();

    private Path sourceFile;

    public DetectedInstance() {
    }

    public DetectedInstance(@Nullable T detectable, @Nullable String typeName, int beginningLineNumber,
                            int endingLineNumber, @Nullable String codeSnippet, @NotNull Path sourceFile) {
        this.codeRanges.add(CodeRange.of(beginningLineNumber, endingLineNumber));
        this.detectable = detectable;
        this.typeName = typeName;
        this.sourceFile = sourceFile;

        if (codeSnippet != null) {
            this.codeSnippets.add(codeSnippet);
        }
    }

    /**
     * Copy constructor.
     *
     * @param detectedInstance The {@link DetectedInstance} to be copied.
     */
    @SuppressWarnings("unchecked")
    public DetectedInstance(@NotNull DetectedInstance<T> detectedInstance) {
        try {
            this.detectable = detectedInstance.detectable != null ? (T) detectedInstance.detectable.clone() : null;
        } catch (CloneNotSupportedException e) {
            this.detectable = null;
        }

        this.typeName = detectedInstance.typeName;
        this.codeRanges.addAll(detectedInstance.codeRanges);
        this.codeSnippets.addAll(detectedInstance.codeSnippets);
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
     * Returns a {@link DetectedInstance<T>}, filled with the position from the given node and the concrete code.
     * No type information is set.
     *
     * @param node     {@link NodeWithRange} to extract positional information.
     * @param detected {@link T} that was detected by a {@link Detector<T>} and should be linked to the {@link DetectedInstance<T>}.
     * @param <T>      The concrete Subtype of {@link Detectable}.
     * @return A {@link DetectedInstance<T>} with the given information.
     */
    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(@NotNull NodeWithRange<?> node, @Nullable T detected) {
        DetectedInstance<T> detectedInstance = new DetectedInstance<>();

        detectedInstance.detectable = detected;
        CodeRange.of(node).ifPresent(detectedInstance.codeRanges::add);
        detectedInstance.codeSnippets.add(node.toString());

        return detectedInstance;
    }

    @NotNull
    public static <T extends Detectable> DetectedInstance<T> from(@NotNull T detected,
                                                                  @NotNull TypeDeclaration<?> typeDeclaration,
                                                                  @NotNull NodeWithRange<?>... perfumedCodeParts) {
        DetectedInstance<T> detectedInstance = new DetectedInstance<>();

        detectedInstance.setDetectable(detected);
        detectedInstance.setTypeName(typeDeclaration.getName().getIdentifier());

        for (NodeWithRange<?> node : perfumedCodeParts) {
            CodeRange.of(node).ifPresent(detectedInstance.codeRanges::add);
            detectedInstance.codeSnippets.add(node.toString());
        }

        return detectedInstance;
    }

    /**
     * Returns a {@link DetectedInstance} filled with positional information, the given {@link T} detectable,
     * and the name of the Type (e.g. Class) that contains the node, extracted from the given AST.<br/>
     * <b>CAUTION:</b><br/>
     * ONLY {@link ClassOrInterfaceDeclaration}s will be detected as the direct parent type of the node,
     * the returned {@link DetectedInstance} will likely have incorrect information if the real parent type is
     * something else! So, only use this method if you are <b>sure</b> that the parent type is a
     * {@link ClassOrInterfaceDeclaration}, or if you don't care about the set type name.
     *
     * @param detectable The {@link Detectable} to set for the {@link DetectedInstance}.
     * @param node An AST node that has a parent, and a range.
     * @param ast The AST where we will search the parent type ((abstract) class/interface) for the node.
     * @return The crafted {@link DetectedInstance}.
     * @param <T> The type of {@link Detectable} to be set.
     * @param <S> Some type of AST node with a range and parent.
     */
    @SuppressWarnings("unchecked")
    public static  <T extends Detectable, S extends NodeWithRange<?> & HasParentNode<?>> DetectedInstance<T> from(
            @NotNull T detectable, @NotNull S node, @NotNull CompilationUnit ast) {
        DetectedInstance<T> detection = DetectedInstance.from(node, detectable);

        Optional<ClassOrInterfaceDeclaration> parentType = node.findAncestor(ClassOrInterfaceDeclaration.class);
        parentType.ifPresentOrElse(
                type -> detection.setTypeName(type.getNameAsString()),
                () -> detection.setTypeName(ast.getPrimaryTypeName().orElse("UNKNOWN"))
        );

        return detection;
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

        // Compare by code ranges
        Iterator<CodeRange> thisIterator = codeRanges.iterator();
        Iterator<CodeRange> otherIterator = other.codeRanges.iterator();

        while (thisIterator.hasNext() && otherIterator.hasNext()) {
            int comparison = thisIterator.next().compareTo(otherIterator.next());

            if (comparison != 0) {
                return comparison;
            }
        }

        // If all elements are equal until one has no more code ranges, prefer the one with more code ranges
        return other.codeRanges.size() - codeRanges.size();
    }
}
