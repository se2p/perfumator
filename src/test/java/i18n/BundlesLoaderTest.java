package i18n;

import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the {@link BundlesLoader}.
 */
class BundlesLoaderTest {

    private static class TestA { }

    private static class TestB { }

    private Bundles resourceHolder;

    private static final BundlesLoader testBundlesLoader = new BundlesLoader("i18n",
            BundlesLoader.STANDARD_PERFUMES_PACKAGE);

    @BeforeEach
    void resetLoadedResources() {
        resourceHolder = new Bundles();
    }

    @Test
    void loadApplicationBundlesWithLocale() {
        testBundlesLoader.loadApplicationBundle(resourceHolder, Locale.GERMAN);

        assertThat(resourceHolder.getApplicationResource("test.prop")).isEqualTo("hallo");
        assertThat(resourceHolder.getApplicationResource("test.fallback")).isEqualTo("oui");
        assertThat(resourceHolder.getApplicationResource("test.only_de")).isEqualTo("deutsch");
    }

    @Test
    void loadPerfumeBundlesWithLocale() {
        testBundlesLoader.loadDetectableBundles(resourceHolder, Locale.GERMAN);

        // Bundle for TestA perfume
        assertThat(resourceHolder.getResource("TestA.perfume.name")).isEqualTo("ein Parfüm");
        assertThat(resourceHolder.getResource("perfume.name", TestA.class)).isEqualTo("ein Parfüm");
        assertThat(resourceHolder.getResource("perfume.source", TestA.class)).isEqualTo("Lösungsmuster");
        assertThat(resourceHolder.getResource("perfume.description", TestA.class)).isEqualTo("cest un parfum");
        assertThat(resourceHolder.getResource("perfume.information", TestA.class)).isNull();

        // Bundle for TestB perfume
        assertThat(resourceHolder.getResource("TestB.perfume.name")).isEqualTo("anderes Parfüm");
        assertThat(resourceHolder.getResource("perfume.name", TestB.class)).isEqualTo("anderes Parfüm");
        assertThat(resourceHolder.getResource("perfume.description", TestB.class)).isEqualTo("noch ein Code Parfüm");
        assertThat(resourceHolder.getResource("perfume.doesnt_exist", TestB.class)).isNull();
    }

    @Test
    void loadBundlesNoLocale() {
        testBundlesLoader.loadApplicationBundle(resourceHolder, null);
        testBundlesLoader.loadDetectableBundles(resourceHolder, null);

        // Application Bundle
        assertThat(resourceHolder.getApplicationResource("test.prop")).isEqualTo("bonjour");
        assertThat(resourceHolder.getApplicationResource("test.fallback")).isEqualTo("oui");
        assertThat(resourceHolder.getApplicationResource("test.only_de")).isNull();

        // Bundle for TestA perfume
        assertThat(resourceHolder.getResource("perfume.name", TestA.class)).isEqualTo("le parfum");
        assertThat(resourceHolder.getResource("perfume.source", TestA.class)).isNull();
        assertThat(resourceHolder.getResource("perfume.description", TestA.class)).isEqualTo("cest un parfum");
        assertThat(resourceHolder.getResource("perfume.information", TestA.class)).isNull();

        // Bundle for TestB perfume
        assertThat(resourceHolder.getResource("perfume.name", TestB.class)).isEqualTo("un parfum");
        assertThat(resourceHolder.getResource("perfume.description", TestB.class)).isEqualTo("oui oui");
        assertThat(resourceHolder.getResource("perfume.doesnt_exist", TestB.class)).isNull();
    }

    @Test
    void loadCliBundlesDefault() {
        testBundlesLoader.loadCliBundle(resourceHolder, Locale.ENGLISH);

        assertThat(resourceHolder.getCliBundle()).isNotNull();
        assertThat(resourceHolder.getCliBundle().getString("hello.world")).isEqualTo("Hello world");
    }
}