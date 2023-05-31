package de.jsilbereisen.perfumator.engine;

import de.jsilbereisen.perfumator.engine.detector.perfume.DummyDetector;
import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.Perfume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PerfumeDetectionEngineTest {

    private static DetectableRegistry<Perfume> registryMock;

    private static Bundles bundlesMock;

    private static final Path SINGLE_JAVA_SOURCE = Path.of("src", "test", "resources", "sources", "java",
            "SomeJavaSourceFile.java");

    private static final Path SINGLE_MALFORMED_JAVA_SOURCE = Path.of("src", "test", "resources", "sources", "java",
            "MalformedJavaFile.java");

    private static final Path SINGLE_NON_JAVA = Path.of("src", "test", "resources", "sources", "other",
            "some_other_file.txt");

    @BeforeAll
    static void setupMocks() {
        registryMock = Mockito.mock(PerfumeRegistry.class);
        when(registryMock.getRegisteredDetectors()).thenReturn(Set.of(new DummyDetector()));

        bundlesMock = Mockito.mock(Bundles.class);
        when(bundlesMock.getApplicationResource(anyString())).thenReturn(" I18N ");
    }

    @Test
    void detectInSingleFile() {
        PerfumeDetectionEngine engine = new PerfumeDetectionEngine(registryMock, null);

        List<DetectedInstance<Perfume>> detectedInstances = engine.detect(SINGLE_JAVA_SOURCE);

        assertThat(detectedInstances).hasSize(1);
        assertThat(detectedInstances.get(0).getDetectable().getName()).isEqualTo("Some Perfume");
    }

    @Test
    void detectInSingleFileUnhappyPaths() {
        PerfumeDetectionEngine engine = new PerfumeDetectionEngine(registryMock, null, bundlesMock);

        assertThatThrownBy(() -> engine.detect(SINGLE_NON_JAVA)).isInstanceOf(IllegalArgumentException.class);
        assertThat(engine.detect(SINGLE_MALFORMED_JAVA_SOURCE)).isEmpty();
    }
}