package de.jsilbereisen.perfumator.model;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.i18n.*;
import de.jsilbereisen.perfumator.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    @I18nIgnore
    private String i18nBaseBundleName;

    /**
     * Default constructor to allow deserialization via the <b>Jackson</b> object mapper.
     */
    protected Detectable() { }

    // TODO: extend doc
    /**
     * Simple name of the {@link Detector} class that is responsible for detecting this {@link Detectable}.
     *
     * @param detectorClassSimpleName Simple class name as string, not fully qualified.
     * @param i18nBaseBundleName The base name of the bundle that should be used for internationalization of this
     *                           {@link Detectable}. Can be {@code null}.
     * @throws IllegalArgumentException If the given detector class name is {@code null} or empty.
     */
     protected Detectable(String name, String description, String detectorClassSimpleName,
                          @Nullable String i18nBaseBundleName) {
         if (StringUtil.anyEmpty(name, description, detectorClassSimpleName)) {
             throw new IllegalArgumentException("Perfume must have a non-null, non-empty name, " +
                     "description and detector class name.");
         }

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

    // TODO: useful toString
}
