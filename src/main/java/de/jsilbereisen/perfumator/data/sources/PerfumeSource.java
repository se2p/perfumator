package de.jsilbereisen.perfumator.data.sources;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public abstract class PerfumeSource {

    private final String sourceName;

    protected PerfumeSource(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public String toString() {
        return sourceName;
    }
}
