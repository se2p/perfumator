package de.jsilbereisen.perfumator.model;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.i18n.I18nIgnore;
import de.jsilbereisen.perfumator.i18n.Internationalizable;
import de.jsilbereisen.perfumator.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class for data objects with information for detectable concepts, e.g. Code Perfumes or Code Smells.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract class Detectable implements Internationalizable {

    private String name;

    private String description;

    @I18nIgnore
    private String detectorClassSimpleName;

    // TODO: extend doc
    /**
     * Simple name of the {@link Detector} class that is responsible for detecting this {@link Detectable}.
     * The detector class must be located in the package <b>de.jsilbereisen.perfumator.engine.detector</b> package
     * in order for it to be instanced in the application engine.
     *
     * @param detectorClassSimpleName Simple class name as string, not fully qualified.
     * @throws IllegalArgumentException If the given detector class name is {@code null} or empty.
     */
     protected Detectable(String name, String description, String detectorClassSimpleName) {
         if (StringUtil.anyEmpty(name, description, detectorClassSimpleName)) {
             throw new IllegalArgumentException("Perfume must have a non-null, non-empty name, " +
                     "description and detector class name.");
         }

         this.name = name;
         this.description = description;
         this.detectorClassSimpleName = detectorClassSimpleName;
    }

    // TODO: useful toString
}
