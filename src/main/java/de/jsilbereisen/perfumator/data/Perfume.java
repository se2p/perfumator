package de.jsilbereisen.perfumator.data;

import de.jsilbereisen.perfumator.data.sources.PerfumeSource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class Perfume {

    private String name;

    private String description;

    private List<String> additionalNotes = new ArrayList<>();

    private PerfumeSource source;

    private Class<?> detector;

    public Perfume() {

    }

    public Perfume(String name, String description, List<String> additionalNotes,
                   PerfumeSource source, Class<?> detector) {
        this.name = name;
        this.description = description;
        this.additionalNotes = additionalNotes;
        this.source = source;
        this.detector = detector;
    }

    // TODO: i18n toString
}
