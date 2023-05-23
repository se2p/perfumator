package de.jsilbereisen.perfumator.data.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.jsilbereisen.perfumator.data.model.Perfume;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * {@link DetectableRegistry} implementation for {@link Perfume}s. Loads Perfume definitions that are
 * provided as JSONs under the {@link #PERFUME_DEFINITIONS_PACKAGE} under the resource directory.<br/>
 * Object mapping is done with the help of the <i>Jackson</i> library. For scanning the package for the
 * JSONs, the <i>ClassGraph</i> library is used (required to also detect resources when packaged as JAR application).
 */
@Slf4j
public class PerfumeRegistry implements DetectableRegistry<Perfume> {

    /**
     * Standard package/directory under the resources directory (e.g. src/main/resources or src/text/resources),
     * where Perfume definitions are located.
     */
    public static final String PERFUME_DEFINITIONS_PACKAGE = "de/jsilbereisen/perfumator/data/perfumes";

    private Map<Perfume, Detector<Perfume>> registry = new HashMap<>();

    // TODO refactor
    @Override
    public void loadRegistry(@NotNull Locale locale) {
        ClassGraph resourceScanner = new ClassGraph().acceptPathsNonRecursive(PERFUME_DEFINITIONS_PACKAGE);
        JsonMapper jsonMapper = new JsonMapper();

        List<Perfume> loadedPerfumes = new ArrayList<>();

        try (ScanResult result = resourceScanner.scan()) {
            ResourceList allPerfumes = result.getAllResources();
            allPerfumes.stream()
                    .filter(resource -> resource.getPath().endsWith(".json"))
                    .forEach(resource -> {
                        String jsonRepresentation = null;

                        try {
                            jsonRepresentation = resource.getContentAsString();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        if (jsonRepresentation != null) {
                            Perfume loadedPerfume = null;

                            try {
                                loadedPerfume = jsonMapper.readValue(jsonRepresentation, Perfume.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }

                            if (loadedPerfume != null) {
                                loadedPerfumes.add(loadedPerfume);
                            }
                        }
                    });
        } catch (ClassGraphException e) {
            // TODO: Define + throw own exception?
            log.error("Could not load Perfumes.");
            throw e;
        }
    }

    @Override
    public List<Perfume> getRegisteredDetectables() {
        return null;
    }

    @Override
    public List<Detector<Perfume>> getRegisteredDetectors() {
        return null;
    }

    @Override
    public Detector<Perfume> getDetector(Perfume detectable) {
        return null;
    }
}
