package it;

import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.io.LanguageTag;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Integration test for loading the defined {@link de.jsilbereisen.perfumator.model.perfume.Perfume}s from their default
 * location with all defined {@link LanguageTag} locales.
 */
class PerfumeRegistryIT {

    @ParameterizedTest
    @EnumSource(LanguageTag.class)
    void loadRegistryAllLanguages(@NotNull LanguageTag languageTag) {
        PerfumeRegistry registry = new PerfumeRegistry();
        Locale i18nLocale = languageTag.getRelatedLocale();

        assertThatNoException()
                .as("Exception when loading Perfumes into Registry with locale " + i18nLocale)
                .isThrownBy(() -> registry.loadRegistry(i18nLocale));
    }
}
