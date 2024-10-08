package i18n;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.I18nIgnore;
import de.jsilbereisen.perfumator.i18n.Internationalizable;
import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Internationalizable}.
 */
class InternationalizableTest {

    private static final Bundles MOCKED_BUNDLES = Mockito.mock(Bundles.class);

    @BeforeAll
    static void setupBundlesMock() {
        when(MOCKED_BUNDLES.getResource("name", I18nMe.class)).thenReturn("bar");
        when(MOCKED_BUNDLES.getResource("missingResource", I18nMe.class)).thenReturn(null);
        when(MOCKED_BUNDLES.getResource("ignored", I18nMe.class)).thenReturn("not ignored");
        when(MOCKED_BUNDLES.getResource("notAString", I18nMe.class)).thenReturn("this will hurts");

        when(MOCKED_BUNDLES.getResource("some_bundle.name")).thenReturn("changed");
        when(MOCKED_BUNDLES.getResource("some_bundle.detectorClassSimpleName")).thenReturn("must not change");
    }

    /**
     * Test the default implementation for internationalisation.
     */
    @Test
    void internationalizeDefaultImpl() {
        I18nMe i18nMe = new I18nMe();

        i18nMe.internationalize(MOCKED_BUNDLES);

        assertThat(i18nMe.name).isEqualTo("bar");
        assertThat(i18nMe.missingResource).isEqualTo("missing");
        assertThat(i18nMe.ignored).isEqualTo("ignored");
        assertThat(i18nMe.notAString).isZero();
    }

    @Test
    void internationalizeDetectable() {
        I18nMeDetectable i18nMeDetectable = new I18nMeDetectable("name", "description",
                "ignored", "some_bundle");

        i18nMeDetectable.internationalize(MOCKED_BUNDLES);

        assertThat(i18nMeDetectable.getName()).isEqualTo("changed");
        assertThat(i18nMeDetectable.getDescription()).isEqualTo("description");
        assertThat(i18nMeDetectable.getDetectorClassSimpleName()).isEqualTo("ignored");
        assertThat(i18nMeDetectable.getI18nBaseBundleName()).isEqualTo("some_bundle");
    }

    @Test
    void internationalizeDetectableNoBundleSet() {
        I18nMeDetectable i18nMeDetectable = new I18nMeDetectable("name", "description",
                "ignored", null);

        i18nMeDetectable.internationalize(MOCKED_BUNDLES);

        assertThat(i18nMeDetectable.getName()).isEqualTo("name");
        assertThat(i18nMeDetectable.getDescription()).isEqualTo("description");
        assertThat(i18nMeDetectable.getDetectorClassSimpleName()).isEqualTo("ignored");
        assertThat(i18nMeDetectable.getI18nBaseBundleName()).isNull();
    }

    @Test
    void testPerfumeI18n() {
        Perfume perfume = new Perfume();
        perfume.setI18nBaseBundleName("bundle");

        Bundles bundles = mock(Bundles.class);
        when(bundles.getResource("bundle.name")).thenReturn("x");
        when(bundles.getResource("bundle.description")).thenReturn("x");
        when(bundles.getResource("bundle.i18nBaseBundleName")).thenReturn("x");
        when(bundles.getResource("bundle.source#1")).thenReturn("Hallo");
        when(bundles.getResource("bundle.source#2")).thenReturn("Test");
        when(bundles.getResource("bundle.additionalInformation")).thenReturn("x");

        perfume.internationalize(bundles);

        assertThat(perfume.getName()).isEqualTo("x");
        assertThat(perfume.getDescription()).isEqualTo("x");
        assertThat(perfume.getDetectorClassSimpleName()).isNull();
        assertThat(perfume.getI18nBaseBundleName()).isEqualTo("bundle");
        assertThat(perfume.getSources()).containsExactly("Hallo", "Test");
        assertThat(perfume.getRelatedPattern()).isNull();
        assertThat(perfume.getAdditionalInformation()).isEqualTo("x");
    }

    @Setter
    public static class I18nMe implements Internationalizable {

        private String name = "foo";

        private String missingResource = "missing";

        @I18nIgnore
        private String ignored = "ignored";

        private int notAString;
    }

    @Setter
    public static class I18nMeDetectable extends Detectable {

        public I18nMeDetectable(@Nullable String name, @Nullable String description,
                                @Nullable String detectorClassSimpleName, @Nullable String i18nBundleBaseName) {
            super(name, description, detectorClassSimpleName, i18nBundleBaseName);
        }

        @Override
        public Detectable clone() {
            return null;
        }
    }
}