package engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import test.dummy.DummyDetector;

import de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine;
import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PerfumeDetectionEngineTest {

    private static final Path SINGLE_JAVA_SOURCE = Path.of("src", "test", "resources", "sources", "java",
            "SomeJavaSourceFile.java");
    private static final Path SINGLE_MALFORMED_JAVA_SOURCE = Path.of("src", "test", "resources", "sources", "java",
            "MalformedJavaFile.java");
    private static final Path SINGLE_NON_JAVA = Path.of("src", "test", "resources", "sources", "other",
            "some_other_file.txt");
    private static final Path DIR_EMPTY = Path.of("src", "test", "resources", "sources", "empty");
    private static final Path DIR_SMALL_PROJECT = Path.of("src", "test", "resources", "sources", "projects",
            "small_project");
    private static DetectableRegistry<Perfume> registryMock;
    private static Bundles bundlesMock;

    @BeforeAll
    static void setupMocks() {
        registryMock = Mockito.mock(PerfumeRegistry.class);
        when(registryMock.getRegisteredDetectors()).thenReturn(Set.of(new DummyDetector()));

        bundlesMock = Mockito.mock(Bundles.class);
        when(bundlesMock.getApplicationResource(anyString())).thenReturn(" I18N ");
    }

    @Test
    void detectInSingleFile() {
        PerfumeDetectionEngine engine = PerfumeDetectionEngine.builder()
                .registry(registryMock)
                .build();

        List<DetectedInstance<Perfume>> detectedInstances = engine.detect(SINGLE_JAVA_SOURCE).getDetections();

        assertThat(detectedInstances).hasSize(1);
        assertThat(detectedInstances.get(0).getDetectable().getName()).isEqualTo("Some Perfume");
    }

    @Test
    void detectInSingleFileUnhappyPaths() {
        PerfumeDetectionEngine engine = PerfumeDetectionEngine.builder()
                .registry(registryMock)
                .i18nResources(bundlesMock)
                .build();

        assertThatThrownBy(() -> engine.detect(SINGLE_NON_JAVA)).isInstanceOf(IllegalArgumentException.class);
        assertThat(engine.detect(SINGLE_MALFORMED_JAVA_SOURCE).getDetections()).isEmpty();
    }

    @Test
    void detectInEmptyDir() {
        PerfumeDetectionEngine engine = PerfumeDetectionEngine.builder()
                .registry(registryMock)
                .i18nResources(bundlesMock)
                .build();

        assertThat(engine.detect(DIR_EMPTY).getDetections()).isEmpty();
    }

    @Test
    void detectInSmallProject() {
        PerfumeDetectionEngine engine = PerfumeDetectionEngine.builder()
                .registry(registryMock)
                .i18nResources(bundlesMock)
                .build();

        List<DetectedInstance<Perfume>> detectedInstances = engine.detect(DIR_SMALL_PROJECT).getDetections();
        assertThat(detectedInstances).hasSize(4);

        List<String> detectedTypesNames = detectedInstances.stream().map(DetectedInstance::getTypeName)
                .collect(Collectors.toList());
        assertThat(detectedTypesNames).containsExactlyInAnyOrder("AnotherDirClass", "SubpackageClassOne",
                "SubpackageClassTwo", "Main");
    }
}