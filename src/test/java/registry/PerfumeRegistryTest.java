package registry;

import org.junit.jupiter.api.Test;
import test.dummy.DummyDetector;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.model.DetectableComparator;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.model.perfume.RelatedPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link PerfumeRegistry} implementation.
 */
class PerfumeRegistryTest {

    /**
     * Test auto-detection and loading of Perfumes in the default location.
     * Also tests whether instantiation of the specified {@link Detector} (here: {@link DummyDetector}) works with
     * the implicit default constructor.
     */
    @Test
    void loadPerfumes() {
        PerfumeRegistry perfumeRegistry = new PerfumeRegistry("perfumes", "test.dummy",
                "i18n", "registry_test");

        perfumeRegistry.loadRegistry(Locale.GERMAN);

        List<Perfume> loadedPerfumes = new ArrayList<>(perfumeRegistry.getRegisteredDetectables());
        loadedPerfumes.sort(new DetectableComparator<>()); // Ensure consistent ordering in the test

        assertThat(loadedPerfumes).hasSize(2);

        Perfume perfumeB = loadedPerfumes.get(0);
        Perfume perfumeA = loadedPerfumes.get(1);

        // Check correct loading of perfume A, no internationalization
        assertThat(perfumeA.getName()).isEqualTo("Perfume A");
        assertThat(perfumeA.getDescription()).isEqualTo("Test description.");
        assertThat(perfumeA.getDetectorClassSimpleName()).isEqualTo("DummyDetector");
        assertThat(perfumeA.getI18nBaseBundleName()).isNull();
        assertThat(perfumeA.getSources()).containsExactly("My head");
        assertThat(perfumeA.getRelatedPattern()).isEqualTo(RelatedPattern.BUG);
        assertThat(perfumeA.getAdditionalInformation()).isNull();

        // Check whether Perfume B has the correct values, as it should get internationalized
        assertThat(perfumeB.getName()).isEqualTo("Parfuem B");
        assertThat(perfumeB.getDescription()).isEqualTo("Anderes Parfuem.");
        assertThat(perfumeB.getDetectorClassSimpleName()).isEqualTo("DummyDetector");
        assertThat(perfumeB.getI18nBaseBundleName()).isEqualTo("TestResources");
        assertThat(perfumeB.getSources()).containsExactly("Mein Kopf");
        assertThat(perfumeB.getRelatedPattern()).isEqualTo(RelatedPattern.BUG);
        assertThat(perfumeB.getAdditionalInformation()).isEqualTo("More information.");

        Set<Detector<Perfume>> registeredDetectors = perfumeRegistry.getRegisteredDetectors();

        assertThat(registeredDetectors).hasSize(1);

        Detector<Perfume> dummyDetector = registeredDetectors.stream().findFirst().orElse(null);
        assertThat(dummyDetector).isInstanceOf(DummyDetector.class);
    }
}