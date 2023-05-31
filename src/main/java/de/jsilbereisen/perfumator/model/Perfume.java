package de.jsilbereisen.perfumator.model;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

/**
 * Data object for a Code Perfume definition.
 * In the application, instances of this class are created from JSON representations.
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Perfume extends Detectable {

    private String source;

    private RelatedPatternType relatedPattern;

    private String additionalInformation;

    /**
     * Default constructor to allow deserialization via the <b>Jackson</b> object mapper.
     */
    public Perfume() { }

    /**
     * Constructor containing all fields.
     * No checks are performed whether a {@link Detector} class with the given name even exists.
     */
    public Perfume(@Nullable String name, @Nullable String description, @Nullable String source,
                   @Nullable RelatedPatternType relatedPattern,
                   @Nullable String additionalInformation,
                   @Nullable String detectorClassSimpleName, @Nullable String i18nBaseBundleName) {
        super(name, description, detectorClassSimpleName, i18nBaseBundleName);

        this.source = source;
        this.relatedPattern = relatedPattern;
        this.additionalInformation = additionalInformation;
    }

    // TODO: i18n toString
}
