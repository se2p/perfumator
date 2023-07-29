package de.jsilbereisen.perfumator.model.perfume;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.model.Detectable;

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

    private RelatedPattern relatedPattern;

    private String additionalInformation;

    /**
     * Default constructor to allow deserialization via the <b>Jackson</b> object mapper.
     */
    public Perfume() {
    }

    /**
     * Constructor containing all fields.
     * No checks are performed whether a {@link Detector} class with the given name even exists.
     */
    public Perfume(@Nullable String name, @Nullable String description, @Nullable String source,
                   @Nullable RelatedPattern relatedPattern,
                   @Nullable String additionalInformation,
                   @Nullable String detectorClassSimpleName, @Nullable String i18nBaseBundleName) {
        super(name, description, detectorClassSimpleName, i18nBaseBundleName);

        this.source = source;
        this.relatedPattern = relatedPattern;
        this.additionalInformation = additionalInformation;
    }

    /**
     * Copy constructor.
     *
     * @param perfume The {@link Perfume} which should be copied.
     */
    public Perfume(@NotNull Perfume perfume) {
        super(perfume);

        this.source = perfume.source;
        this.relatedPattern = perfume.relatedPattern;
        this.additionalInformation = perfume.additionalInformation;
    }

    // TODO: toString

    @Override
    public int compareTo(@NotNull Detectable other) {
        if (!(other instanceof Perfume otherPerfume)) {
            return super.compareTo(other);
        }

        // Order by the related pattern. If missing, put at the end.
        int relatedPatternComparison;
        if (relatedPattern != null) {
            relatedPatternComparison = otherPerfume.relatedPattern != null
                    ? relatedPattern.compareTo(otherPerfume.relatedPattern)
                    : -1; // Other has no related pattern => this has priority
        } else {
            relatedPatternComparison = otherPerfume.relatedPattern != null ? 1 : 0;
        }
        if (relatedPatternComparison != 0) {
            return relatedPatternComparison;
        }

        // compare through super class
        return super.compareTo(other);
    }

}
