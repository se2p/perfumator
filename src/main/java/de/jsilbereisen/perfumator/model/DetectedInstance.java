package de.jsilbereisen.perfumator.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Detectable} instance that was detected in a source file.
 * Holds meta information on the detection location etc.
 */
@Getter
@EqualsAndHashCode
public class DetectedInstance<T extends Detectable> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "detectableClass")
    private final T detectable;

    private final String className;

    private final int lineNumber;

    private final String concreteCode;

    public DetectedInstance(@NotNull T detectable, @NotNull String className, int lineNumber,
                            @NotNull String concreteCode) {
        this.detectable = detectable;
        this.className = className;
        this.lineNumber = lineNumber;
        this.concreteCode = concreteCode;
    }

    @Override
    public String toString() {
        if (detectable == null) {
            return "No information";
        }

        String ret = "Detected \"" + detectable.getName() + "\"";

        if (className != null) {
            ret += " in class \"" + className + "\", line " + lineNumber;
        }

        return ret;
    }

    // TODO: i18n
}
