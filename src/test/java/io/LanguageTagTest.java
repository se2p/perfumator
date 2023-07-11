package io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.jsilbereisen.perfumator.io.LanguageTag;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for comfort-methods in the {@link LanguageTag} enum.
 */
class LanguageTagTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "enen", "ENEN", " NE "})
    void equalsIgnoreCaseUnhappy(String other) {
        assertThat(LanguageTag.EN.equalsTrimIgnoreCase(other)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "EN", " eN  "})
    void equalsIgnoreCaseHappy(String other) {
        assertThat(LanguageTag.EN.equalsTrimIgnoreCase(other)).isTrue();
    }

    /**
     * Test that the standard language tag names in the class are recognized.
     */
    @Test
    void languageTagOf() {
        assertThat(LanguageTag.of("en")).isEqualTo(LanguageTag.EN);
        assertThat(LanguageTag.of("de")).isEqualTo(LanguageTag.DE);
        assertThat(LanguageTag.of("fr")).isEqualTo(LanguageTag.FR);
        assertThat(LanguageTag.of("es")).isEqualTo(LanguageTag.ES);
        assertThat(LanguageTag.of("it")).isEqualTo(LanguageTag.IT);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "unknown"})
    void languageTagOfFallback(String str) {
        assertThat(LanguageTag.of(str)).isEqualTo(LanguageTag.EN);
    }
}