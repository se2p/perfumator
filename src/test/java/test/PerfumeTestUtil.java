package test;

import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.model.perfume.RelatedPattern;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

public final class PerfumeTestUtil {

    public static final Path EXAMPLE_PROJECT_DIR_STRUCTURE = Path.of("src", "main", "java", "de", "jsilbereisen");

    private static final Perfume SINGLE_EXAMPLE_PERFUME = new Perfume("Example", "Example description",
            List.of("Example source"), RelatedPattern.BUG, "Example info", "ExampleDetector", "exampleBundleName");

    private static final DetectedInstance<Perfume> SINGLE_EXAMPLE_DETECTED_INSTANCE = new DetectedInstance<>(SINGLE_EXAMPLE_PERFUME,
            "ExampleClass", 10, 20, Path.of("")).setSourceFile(null);

    private PerfumeTestUtil() {
    }

    @NotNull
    public static Perfume singleExamplePerfume() {
        return new Perfume(SINGLE_EXAMPLE_PERFUME);
    }

    @NotNull
    public static DetectedInstance<Perfume> singleExampleDetectedInstance() {
        return new DetectedInstance<>(SINGLE_EXAMPLE_DETECTED_INSTANCE);
    }

    @NotNull
    public static PerfumeRegistry mockedRegistryWithExamplePerfume() {
        PerfumeRegistry registry = Mockito.mock(PerfumeRegistry.class);
        when(registry.getRegisteredDetectables()).thenReturn(Set.of(singleExamplePerfume()));

        return registry;
    }

    @NotNull
    public static DetectableRegistry<Perfume> mockedRegistryWithPerfumes(Perfume... perfumes) {
        DetectableRegistry<Perfume> registry = Mockito.mock(PerfumeRegistry.class);
        when(registry.getRegisteredDetectables()).thenReturn(Set.of(perfumes));

        return registry;
    }
}
