package de.jsilbereisen.perfumator.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the {@link BundlesLoader}.
 */
class BundlesLoaderTest {

    private static final Path TEST_RESOURCES = Paths.get("src", "test", "resources");

    private static class TestA { }

    private static class TestB { }

    @Test
    void testLoadApplicationBundlesWithLocale() throws IOException {
        BundlesLoader.loadBundles(Locale.GERMAN, TEST_RESOURCES);

        assertThat(Bundles.getApplicationResource("test.prop")).isEqualTo("hallo");
        assertThat(Bundles.getApplicationResource("test.fallback")).isEqualTo("oui");
        assertThat(Bundles.getApplicationResource("test.only_de")).isEqualTo("deutsch");
    }

    @Test
    void testLoadPerfumeBundlesWithLocale() throws IOException {
        BundlesLoader.loadBundles(Locale.GERMAN, TEST_RESOURCES);

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
    void testLoadBundlesNoLocale() throws IOException {
        BundlesLoader.loadBundles(null, TEST_RESOURCES);

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
}