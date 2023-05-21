package de.jsilbereisen.perfumator.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the {@link BundlesLoader}.
 */
class BundlesLoaderTest {

    private static class TestA { }

    private static class TestB { }

    @AfterEach
    void cleanup() {
        Bundles.resetResources();
    }

    @Test
    void loadApplicationBundlesWithLocale() {
        BundlesLoader.loadApplicationBundle(Locale.GERMAN);

        assertThat(Bundles.getApplicationResource("test.prop")).isEqualTo("hallo");
        assertThat(Bundles.getApplicationResource("test.fallback")).isEqualTo("oui");
        assertThat(Bundles.getApplicationResource("test.only_de")).isEqualTo("deutsch");
    }

    @Test
    void loadPerfumeBundlesWithLocale() {
        BundlesLoader.loadPerfumeBundles(Locale.GERMAN);

        // Bundle for TestA perfume
        assertThat(Bundles.getResource("TestA.perfume.name")).isEqualTo("ein Parfüm");
        assertThat(Bundles.getResource("perfume.name", TestA.class)).isEqualTo("ein Parfüm");
        assertThat(Bundles.getResource("perfume.source", TestA.class)).isEqualTo("Lösungsmuster");
        assertThat(Bundles.getResource("perfume.description", TestA.class)).isEqualTo("cest un parfum");
        assertThat(Bundles.getResource("perfume.information", TestA.class)).isNull();

        // Bundle for TestB perfume
        assertThat(Bundles.getResource("TestB.perfume.name")).isEqualTo("anderes Parfüm");
        assertThat(Bundles.getResource("perfume.name", TestB.class)).isEqualTo("anderes Parfüm");
        assertThat(Bundles.getResource("perfume.description", TestB.class)).isEqualTo("noch ein Code Parfüm");
        assertThat(Bundles.getResource("perfume.doesnt_exist", TestB.class)).isNull();
    }

    @Test
    void loadBundlesNoLocale() {
        BundlesLoader.loadApplicationBundle(null);
        BundlesLoader.loadPerfumeBundles(null);

        // Application Bundle
        assertThat(Bundles.getApplicationResource("test.prop")).isEqualTo("bonjour");
        assertThat(Bundles.getApplicationResource("test.fallback")).isEqualTo("oui");
        assertThat(Bundles.getApplicationResource("test.only_de")).isNull();

        // Bundle for TestA perfume
        assertThat(Bundles.getResource("perfume.name", TestA.class)).isEqualTo("le parfum");
        assertThat(Bundles.getResource("perfume.source", TestA.class)).isNull();
        assertThat(Bundles.getResource("perfume.description", TestA.class)).isEqualTo("cest un parfum");
        assertThat(Bundles.getResource("perfume.information", TestA.class)).isNull();

        // Bundle for TestB perfume
        assertThat(Bundles.getResource("perfume.name", TestB.class)).isEqualTo("un parfum");
        assertThat(Bundles.getResource("perfume.description", TestB.class)).isEqualTo("oui oui");
        assertThat(Bundles.getResource("perfume.doesnt_exist", TestB.class)).isNull();
    }

    @Test
    void loadCliBundlesDefault() {
        BundlesLoader.loadCliBundle(Locale.ENGLISH);
        assertThat(Bundles.getCliBundle().getString("hello.world")).isEqualTo("Hello world");
    }
}