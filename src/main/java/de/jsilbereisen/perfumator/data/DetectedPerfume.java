package de.jsilbereisen.perfumator.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class DetectedPerfume {

    private Perfume perfume;

    private String className;

    private int lineNumber;

    public DetectedPerfume() {}

    public DetectedPerfume(@Nullable Perfume perfume, @Nullable String className, int lineNumber) {
        this.perfume = perfume;
        this.className = className;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        if (perfume == null) {
            return "No information";
        }

        String ret = "Detected \"" + perfume.getName() + "\"";

        if (className != null) {
            ret += " in class \"" + className + "\", line " + lineNumber;
        }

        return ret;
    }

    // TODO: i18n
}
