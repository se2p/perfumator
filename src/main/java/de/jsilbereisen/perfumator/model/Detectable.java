package de.jsilbereisen.perfumator.model;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.i18n.*;
import de.jsilbereisen.perfumator.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Abstract class for data objects with information for detectable concepts, e.g. Code Perfumes or Code Smells.
 * In the context of the <b>Perfumator</b> application, instances of deriving classes of this class are intended
 * to be loaded from JSON-representations with the help of the <i>Jackson</i> object mapper.
 * <br/>
 * Every subclass of {@link Detectable} should also implement {@link #equals} and {@link #hashCode}.
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public abstract class Detectable implements Internationalizable, Comparable<Detectable> {

    private String name;

    private String description;

    @I18nIgnore
    private String detectorClassSimpleName;

    @I18nIgnore
    private String i18nBaseBundleName;

    /**
     * Default constructor to allow deserialization via the <b>Jackson</b> object mapper.
     */
    protected Detectable() { }

    /**
     * Constructor for a {@link Detectable} with a name, description, {@link Detector} class name and bundle name for
     * internationalisation.
     * No checks are performed whether a {@link Detector} class with the given name even exists.
     *
     * @param name Name for the detectable.
     * @param description A description for this detectable
     * @param detectorClassSimpleName Simple class name of the {@link Detector} that is responsible to detect this
     *                                {@link Detectable}, not fully qualified.
     * @param i18nBaseBundleName The base name of the bundle that should be used for internationalization of this
     *                           detectable.
     */
     protected Detectable(@Nullable String name, @Nullable String description,
                          @Nullable String detectorClassSimpleName, @Nullable String i18nBaseBundleName) {
         this.name = name;
         this.description = description;
         this.detectorClassSimpleName = detectorClassSimpleName;
         this.i18nBaseBundleName = i18nBaseBundleName;
    }

    /**
     * <p>
     * Sets all internationalizable fields of the calling instance to their internationalized content, if available.
     * The resource is queried using the {@link #i18nBaseBundleName}, dot-concatenated with the field name.
     * If the {@link #i18nBaseBundleName} is {@code null}, the method immediately returns without any action.
     * If no internationalized resource for the field is available, the field's content does not change.<br/>
     * A field is seen as internationalizable if its type is {@link String} and if it not explicitly excluded
     * from internationalization, meaning it is not annotated with {@link I18nIgnore}.
     * </p>
     * <p>
     * This method uses reflection and relies on the usual naming for setters, e.g. for field "foo", the setter is named
     * "setFoo" (set + field name, first letter of field's name is capitalized).
     * </p>
     *
     * @param resourceHolder {@link Bundles} with resources for internationalization. Make sure to load resources
     *                                      into this instance via {@link BundlesLoader#loadDetectableBundles} before,
     *                                      otherwise the call to this method won't have any effect.
     * @throws InternationalizationException If a problem occurs when calling the detected Setters for the
     *                                       internationalizable fields.
     */
    @Override
    public void internationalize(@NotNull Bundles resourceHolder) {
        if (i18nBaseBundleName == null) {
            // No internationalization bundle is set => just ignore
            return;
        }

        Class<? extends Internationalizable> clazz = getClass();
        Field[] classFields = FieldUtils.getAllFields(getClass());
        Method[] classMethods = clazz.getMethods();

        for (Field field: classFields) {
            if (field.getType().equals(String.class) && !field.isAnnotationPresent(I18nIgnore.class)) {
                String fieldName = field.getName();
                String internationalizedContent = resourceHolder.getResource(i18nBaseBundleName + "." + fieldName);

                if (!StringUtil.isEmpty(internationalizedContent)) {
                    String setterName = "set" + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);

                    for (Method method: classMethods) {
                        if (method.getName().equals(setterName)) {
                            try {
                                method.invoke(this, internationalizedContent);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                throw new InternationalizationException(
                                        String.format("Invocation of setter \"%s\" for Field \"%s\" of class "
                                                + "\"%s\" failed.", setterName, fieldName, clazz.getSimpleName()),
                                        e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Compares {@code this} to the {@link Detectable} {@code other} by their names.<br/>
     * <b>Note:</b> the ordering is inconsistent with {@link #equals}.
     */
    @Override
    public int compareTo(@NotNull Detectable other) {
        if (this.getName() == null) {
            if (other.getName() == null) {
                return 0;
            } else {
                return -1;
            }
        }

        return this.getName().compareTo(other.getName());
    }

    // TODO: useful toString
}
