package de.jsilbereisen.perfumator.i18n;

import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Internationalizable}.
 */
class InternationalizableTest {

    @Setter
    private static class I18nMe implements Internationalizable {

        private String name = "foo";

        private String missingResource = "missing";

        @I18nIgnore
        private String ignored = "ignored";

        private int notAString;
    }

    /**
     * Test the default implementation for internationalisation.
     */
    @Test
    void internationalize() {
        I18nMe i18nMe = new I18nMe();

        try (MockedStatic<Bundles> bundles = Mockito.mockStatic(Bundles.class)) {
            bundles.when(() -> Bundles.getResource("name", I18nMe.class)).thenReturn("bar");
            bundles.when(() -> Bundles.getResource("missingResource", I18nMe.class)).thenReturn(null);
            bundles.when(() -> Bundles.getResource("ignored", I18nMe.class)).thenReturn("not ignored");
            bundles.when(() -> Bundles.getResource("notAString", I18nMe.class)).thenReturn("this will hurts");

            i18nMe.internationalize();

            assertThat(i18nMe.name).isEqualTo("bar");
            assertThat(i18nMe.missingResource).isEqualTo("missing");
            assertThat(i18nMe.ignored).isEqualTo("ignored");
            assertThat(i18nMe.notAString).isZero();
        }
    }
}