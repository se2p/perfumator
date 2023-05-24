package de.jsilbereisen.perfumator.engine.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.jsilbereisen.perfumator.engine.detector.DetectorLoadException;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import de.jsilbereisen.perfumator.model.Perfume;
import de.jsilbereisen.perfumator.model.PerfumeLoadException;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.i18n.Internationalizable;
import de.jsilbereisen.perfumator.util.StringUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassGraphException;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: i18n
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

    public static final String PERFUME_DETECTORS_PACKAGE = "de.jsilbereisen.perfumator.engine.detector.perfume";

    private final Map<Perfume, Detector<Perfume>> registry = new HashMap<>();

    // TODO: i18n exception messages
    /**
     * Detects and loads all Perfumes that are in the {@link #PERFUME_DEFINITIONS_PACKAGE} from their JSONs into the
     * registry and links them with their respective {@link Detector}.
     * Also performs internationalization for each loaded {@link Perfume} with the given {@link Locale},
     * if there are resources available.
     *
     * @param locale The locale to use for loading internationalized versions of the Perfumes.
     * @throws PerfumeLoadException If any failures occur within the detection and loading process.
     */
    @Override
    public void loadRegistry(@NotNull Locale locale) {
        Bundles bundles = new Bundles();
        BundlesLoader.loadPerfumeBundles(bundles, locale);
        BundlesLoader.loadApplicationBundle(bundles, locale);

        List<Perfume> loadedPerfumes = loadPerfumes(bundles);
        if (loadedPerfumes.isEmpty()) {
            log.warn("No perfumes loaded. Check the Registry configuration.");
            return;
        }

        loadedPerfumes.forEach(perfume -> perfume.internationalize(bundles));

        String loadedPerfumeNames = StringUtil.joinStrings(loadedPerfumes.stream().map(Perfume::getName).toList(),
                "\", \"");
        log.info("Loaded Perfumes (internationalized): [\"" + loadedPerfumeNames + "\"]");

        linkPerfumesToDetectors(bundles, loadedPerfumes);
    }

    /**
     * Scans the {@link #PERFUME_DEFINITIONS_PACKAGE} for all JSONs and tries to load a {@link Perfume}
     * instance for each of those, with the help of the <i>Jackson</i> object mapper.
     *
     * @param bundles Resources for internationalized exception messages.
     * @return The list of loaded Perfumes. Never {@code null}, might be empty.
     * @throws PerfumeLoadException If anything goes wrong in the process.
     */
    private @NotNull List<Perfume> loadPerfumes(@NotNull Bundles bundles) {
        List<Perfume> loadedPerfumes = new ArrayList<>();
        ClassGraph resourceScanner = new ClassGraph().acceptPathsNonRecursive(PERFUME_DEFINITIONS_PACKAGE);
        JsonMapper jsonMapper = new JsonMapper();

        try (ScanResult result = resourceScanner.scan()) {
            ResourceList allPerfumes = result.getAllResources();
            allPerfumes.stream()
                    .filter(resource -> resource.getPath().endsWith(".json"))
                    .forEach(resource -> {
                        Perfume perfume = loadSinglePerfume(bundles, resource, jsonMapper);

                        if (perfume != null) {
                            loadedPerfumes.add(perfume);
                        } else {
                            throw new PerfumeLoadException("Unable to load Perfume from resource, reason unknown: " + resource.getPath());
                        }
                    });
        } catch (ClassGraphException e) {
            throw new PerfumeLoadException("Unable to scan for Perfume resources.", e);
        }

        return loadedPerfumes;
    }

    private @Nullable Perfume loadSinglePerfume(@NotNull Bundles resourceHolder, @NotNull Resource resource,
                                                @NotNull JsonMapper jsonMapper) {
        String jsonRepresentation;
        Perfume loadedPerfume = null;

        try {
            jsonRepresentation = resource.getContentAsString();
        } catch (IOException e) {
            throw new PerfumeLoadException("Unable to read file content when trying to load Perfume from resource: "
                    + resource.getPath(), e);
        }

        if (jsonRepresentation != null) {
            try {
                loadedPerfume = jsonMapper.readValue(jsonRepresentation, Perfume.class);
            } catch (JsonProcessingException e) {
                throw new PerfumeLoadException("Unable to map JSON representation to Perfume: " + resource.getPath(),
                        e);
            }
        }

        return loadedPerfume;
    }

    /**
     * Instantiates the respective {@link Detector} for each of the given {@link Perfume}s.
     * The detector is loaded by the fully qualified class name, given by
     * {@link Perfume#getDetectorClassSimpleName()}, with the {@link #PERFUME_DETECTORS_PACKAGE} prepended.
     *
     * @param bundles Resources for internationalized exception messages.
     * @param loadedPerfumes The Perfumes for which the detectors should be instanced.
     * @throws DetectorLoadException When being unable to instantiate the {@link Detector} for a {@link Perfume}
     *                               or when simply no {@link Detector} is found for it.
     */
    @SuppressWarnings("unchecked")
    private void linkPerfumesToDetectors(@NotNull Bundles bundles, @NotNull List<Perfume> loadedPerfumes) {
        for (Perfume perfume: loadedPerfumes) {
            Class<?> detectorClass = null;
            try {
                detectorClass = Class.forName(PERFUME_DETECTORS_PACKAGE + "."
                        + perfume.getDetectorClassSimpleName()).asSubclass(Detector.class);
            } catch (ClassNotFoundException e) {
                throw new DetectorLoadException("Unable to find detector class for Perfume \""
                        + perfume.getName() + "\" with class name \"" + perfume.getDetectorClassSimpleName() + "\"",
                        e);
            }

            // TODO: test with implicit constructor
            Detector<Perfume> detector = null;
            try {
                detector = (Detector<Perfume>) detectorClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException
                     | IllegalAccessException
                     | InvocationTargetException
                     | NoSuchMethodException e) {
                throw new DetectorLoadException("Unable to instantiate detector for Perfume \""
                        + perfume.getName() + "\" with class name \"" + perfume.getDetectorClassSimpleName() + "\"",
                        e);
            }

            registry.put(perfume, detector);
        }
    }

    @Override
    public List<Perfume> getRegisteredDetectables() {
        return new ArrayList<>(registry.keySet());
    }

    @Override
    public List<Detector<Perfume>> getRegisteredDetectors() {
        return new ArrayList<>(registry.values());
    }

    @Override
    public Detector<Perfume> getDetector(@NotNull Perfume detectable) {
        return registry.get(detectable);
    }
}
