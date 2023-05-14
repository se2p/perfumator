package de.jsilbereisen.perfumator.data.sources;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DefaultSource extends PerfumeSource {

    private String additionalInformation;

    public DefaultSource() {
        super("No explicit source"); // TODO: i18n
    }

    public DefaultSource(@Nullable String sourceName, @Nullable String additionalInformation) {
        super(sourceName);

        this.additionalInformation = additionalInformation;
    }

    @Override
    public String toString() {
        String ret = getSourceName() != null ? getSourceName() : "No explicit source";

        if (additionalInformation != null) {
            ret += "\n" + "Additional information: " + additionalInformation;
        }

        return ret;
    }

    // TODO: i18n
}
