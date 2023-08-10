package de.jsilbereisen.perfumator.i18n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * <p>
 *     Annotation to provide information about how a field of type {@link List} is internationalized.
 * </p>
 * <p>
 *     <b>Must only be used on {@link String} lists.</b>
 * </p>
 * <p>
 *     The {@link #key()} and {@link #enumerationSuffix()} define what the keys in the {@link Bundles}
 *     must look like for the annotated list. List elements (= keys in the bundle) must have ongoing numbers,
 *     starting with "1", so that the correct order of the items is preserved.
 * </p>
 * <p>
 *     E.g., suffix "#" and key "elem", we have "elem#1", "elem#2", "elem#3" and so on as valid keys in the {@link Bundles}.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface I18nList {

    /**
     * Base key for a list element in the internationalization {@link Bundles}.
     */
    String key();

    /**
     * Suffix to connect the {@link #key()} with the ongoing enumeration number.
     */
    String enumerationSuffix() default "#";
}
