package de.jsilbereisen.perfumator.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Collection of language tags that are (potentially) supported.
 * Can be added to at will if you miss any languages or tags.
 */
public enum LanguageTag {

    /**
     * English. Used as a default in the application.
     */
    EN("en", "English", Locale.ENGLISH),

    /**
     * German.
     */
    DE("de", "Deutsch", Locale.GERMAN),

    /**
     * French.
     */
    FR("fr", "Francais", Locale.FRENCH),

    /**
     * Spanish.
     */
    ES("es", "Espanol", Locale.forLanguageTag("es")),

    /**
     * Italian.
     */
    IT("it", "Italiano", Locale.ITALIAN);

    private final String tagName;

    private final String fullLanguageName;

    private final Locale relatedLocale;

    LanguageTag(String tagName, String fullLanguageName, Locale relatedLocale) {
        this.tagName = tagName;
        this.fullLanguageName = fullLanguageName;
        this.relatedLocale = relatedLocale;
    }

    /**
     * Returns the language tag that is used as a default.
     */
    public static LanguageTag getDefault() {
        return LanguageTag.EN;
    }

    /**
     * Returns the matching {@link LanguageTag}, matched by {@link LanguageTag#getTagName()}, for the given
     * String. If no particular {@link LanguageTag} matches, {@link LanguageTag#EN} is returned.
     *
     * @param str String to match against the available tags. Can be {@code null}.
     * @return The matching {@link LanguageTag}, or {@link LanguageTag#EN} if no match was found.
     */
    public static @NotNull LanguageTag of(@Nullable String str) {
        return Arrays.stream(LanguageTag.values())
                .filter(languageTag -> languageTag.equalsTrimIgnoreCase(str))
                .findFirst()
                .orElse(LanguageTag.EN);
    }

    /**
     * Returns the matching {@link LanguageTag}, matched by {@link LanguageTag#getRelatedLocale()},
     * for the given {@link Locale}.
     * If no particular {@link LanguageTag} matches, {@link LanguageTag#EN} is returned.
     *
     * @param locale {@link Locale} to match against the available tags. Can be {@code null}.
     * @return The matching {@link LanguageTag}, or {@link LanguageTag#EN} if no match was found.
     */
    public static @NotNull LanguageTag of(@Nullable Locale locale) {
        return Arrays.stream(LanguageTag.values())
                .filter(languageTag -> languageTag.relatedLocale.equals(locale))
                .findFirst()
                .orElse(LanguageTag.EN);
    }

    /**
     * Returns an unmodifiable {@link List} with the tag names of all {@link LanguageTag}s.
     */
    public static @NotNull List<String> getAllTagNames() {
        return Arrays.stream(LanguageTag.values())
                .map(LanguageTag::getTagName)
                .toList();
    }

    public String getTagName() {
        return tagName;
    }

    public String getFullLanguageName() {
        return fullLanguageName;
    }

    public Locale getRelatedLocale() {
        return relatedLocale;
    }

    /**
     * Returns {@code true} if the other String equals the language tag's name of {@code this} after
     * trimming and ignoring case.
     */
    public boolean equalsTrimIgnoreCase(@Nullable String other) {
        return other != null && other.trim().equalsIgnoreCase(tagName);
    }
}
