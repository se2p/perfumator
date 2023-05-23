package de.jsilbereisen.perfumator.data;

import de.jsilbereisen.perfumator.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data object for a Code Perfume.
 */
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class Perfume {

    private String name;

    private String description;

    private String source;

    private RelatedPatternType relatedPattern;

    private List<String> additionalInformation = new ArrayList<>();

    private Class<?> detector;

    public Perfume() {

    }

    /**
     * Constructor containing all fields. Note that the {@code name}, {@code description}
     * must neither be {@code null} or empty (see {@link de.jsilbereisen.perfumator.util.StringUtil#isEmpty(String)})
     * Also, the {@code detector} parameter must not be {@code null}.
     */
    public Perfume(@NotNull String name, @NotNull String description, @Nullable String source,
                   @Nullable RelatedPatternType relatedPattern,
                   @Nullable List<String> additionalInformation,
                   @NotNull Class<?> detector) {
        if (StringUtil.anyEmpty(name, description) || detector == null) {
            throw new IllegalArgumentException("Perfume must have a non-null, non-empty name and " +
                    "description, and a non-null detector class.");
        }

        this.name = name;
        this.description = description;
        this.source = source;
        this.relatedPattern = relatedPattern;
        this.additionalInformation = additionalInformation;
        this.detector = detector;
    }

    // TODO: i18n toString
}
