package de.jsilbereisen.perfumator.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * Data object for a Code Perfume definition.
 * In the application, instances of this are created from JSON representations.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Perfume extends Detectable {

    private String source;

    private RelatedPatternType relatedPattern;

    private String additionalInformation;

    /**
     * Constructor containing all fields. Note that the {@code name}, {@code description} and {@code detectorClassSimpleName}
     * parameters must neither be {@code null} or empty (see {@link de.jsilbereisen.perfumator.util.StringUtil#isEmpty(String)}.
     *
     * @throws IllegalArgumentException If the given name, description or detector class name is {@code null} or empty.
     */
    public Perfume(String name, String description, @Nullable String source,
                   @Nullable RelatedPatternType relatedPattern,
                   @Nullable String additionalInformation,
                   String detectorClassSimpleName, @Nullable String i18nBaseBundleName) {
        super(name, description, detectorClassSimpleName, i18nBaseBundleName);

        this.source = source;
        this.relatedPattern = relatedPattern;
        this.additionalInformation = additionalInformation;
    }

    // TODO: i18n toString
}
