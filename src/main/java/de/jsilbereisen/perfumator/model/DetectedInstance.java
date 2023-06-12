package de.jsilbereisen.perfumator.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
}
