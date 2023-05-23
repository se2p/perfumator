package de.jsilbereisen.perfumator.i18n;

import de.jsilbereisen.perfumator.util.StringUtil;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Interface for internationalizable data objects.
 * Uses the {@link Bundles} class to access the loaded resources for internationalization,
 * so make sure to first load the required resources at some point through calls to the {@link BundlesLoader}, before
 * calling {@link #internationalize()}.
 */
public interface Internationalizable {

    /**
     * <p>
     * Sets all internationalizable fields of the calling instance to their internationalized content, if available.
     * If no internationalized resource for the field is available, the field's content does not change.<br/>
     * A field is seen as internationalizable if its type is {@link String} and if it not explicitly excluded
     * from internationalization, meaning it is not annotated with {@link I18nIgnore}.
     * </p>
     * <p>
     * This method uses reflection and relies on the usual naming for setters, e.g. for field "foo", the setter is named
     * "setFoo" (set + field name, first letter of field's name is capitalized).
     * </p>
     *
     * @throws RuntimeException If a problem occurs when calling the detected Setters for the internationalizable fields.
     */
    default void internationalize() {
        Class<? extends Internationalizable> clazz = getClass();
        Field[] classFields = FieldUtils.getAllFields(getClass());
        Method[] classMethods = clazz.getMethods();

        for (Field field: classFields) {
            if (field.getType().equals(String.class) && !field.isAnnotationPresent(I18nIgnore.class)) {
                String fieldName = field.getName();
                String internationalizedContent = Bundles.getResource(fieldName, clazz);

                if (!StringUtil.isEmpty(internationalizedContent)) {
                    String setterName = "set" + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);

                    for (Method method: classMethods) {
                        if (method.getName().equals(setterName)) {
                            try {
                                method.invoke(this, internationalizedContent);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }


    }
}