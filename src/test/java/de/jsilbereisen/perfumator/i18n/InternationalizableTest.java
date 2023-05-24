package de.jsilbereisen.perfumator.i18n;

import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    private static final Bundles MOCKED_BUNDLES = Mockito.mock(Bundles.class);

    @BeforeAll
    static void setupBundlesMock() {
        when(MOCKED_BUNDLES.getResource("name", I18nMe.class)).thenReturn("bar");
        when(MOCKED_BUNDLES.getResource("missingResource", I18nMe.class)).thenReturn(null);
        when(MOCKED_BUNDLES.getResource("ignored", I18nMe.class)).thenReturn("not ignored");
        when(MOCKED_BUNDLES.getResource("notAString", I18nMe.class)).thenReturn("this will hurts");
    }

    /**
     * Test the default implementation for internationalisation.
     */
    @Test
    void internationalize() {
        I18nMe i18nMe = new I18nMe();

        i18nMe.internationalize(MOCKED_BUNDLES);

        assertThat(i18nMe.name).isEqualTo("bar");
        assertThat(i18nMe.missingResource).isEqualTo("missing");
        assertThat(i18nMe.ignored).isEqualTo("ignored");
        assertThat(i18nMe.notAString).isZero();
    }
}