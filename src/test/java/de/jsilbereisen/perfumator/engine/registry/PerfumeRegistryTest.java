package de.jsilbereisen.perfumator.engine.registry;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.detector.perfume.DummyDetector;
import de.jsilbereisen.perfumator.model.Perfume;
import de.jsilbereisen.perfumator.model.RelatedPatternType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class PerfumeRegistryTest {

    @Test
    void loadPerfumes() {
        PerfumeRegistry perfumeRegistry = new PerfumeRegistry();

        perfumeRegistry.loadRegistry(Locale.GERMAN);

        List<Perfume> loadedPerfumes = perfumeRegistry.getRegisteredDetectables();
        assertThat(loadedPerfumes).hasSize(2);

        Perfume perfumeB = loadedPerfumes.get(0);
        Perfume perfumeA = loadedPerfumes.get(1);

        // Check correct loading of perfume A, no internationalization
        assertThat(perfumeA.getName()).isEqualTo("Perfume A");
        assertThat(perfumeA.getDescription()).isEqualTo("Test description.");
        assertThat(perfumeA.getDetectorClassSimpleName()).isEqualTo("DummyDetector");
        assertThat(perfumeA.getI18nBaseBundleName()).isNull();
        assertThat(perfumeA.getSource()).isEqualTo("My head");
        assertThat(perfumeA.getRelatedPattern()).isEqualTo(RelatedPatternType.BUG);
        assertThat(perfumeA.getAdditionalInformation()).isNull();

        // Check whether Perfume B has the correct values, as it should get internationalized
        assertThat(perfumeB.getName()).isEqualTo("Parfuem B");
        assertThat(perfumeB.getDescription()).isEqualTo("Anderes Parfuem.");
        assertThat(perfumeB.getDetectorClassSimpleName()).isEqualTo("DummyDetector");
        assertThat(perfumeB.getI18nBaseBundleName()).isEqualTo("TestResources");
        assertThat(perfumeB.getSource()).isEqualTo("Mein Kopf");
        assertThat(perfumeB.getRelatedPattern()).isEqualTo(RelatedPatternType.BUG);
        assertThat(perfumeB.getAdditionalInformation()).isEqualTo("More information.");

        List<Detector<Perfume>> registeredDetectors = perfumeRegistry.getRegisteredDetectors();

        assertThat(registeredDetectors).hasSize(1);

        Detector<Perfume> dummyDetector = registeredDetectors.get(0);
        assertThat(dummyDetector).isInstanceOf(DummyDetector.class);
    }
}