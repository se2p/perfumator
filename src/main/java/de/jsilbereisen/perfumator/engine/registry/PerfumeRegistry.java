package de.jsilbereisen.perfumator.engine.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.jsilbereisen.perfumator.engine.detector.DetectorLoadException;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.model.perfume.PerfumeLoadException;
import de.jsilbereisen.perfumator.engine.detector.Detector;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// TODO: i18n
/**
 * {@link DetectableRegistry} implementation for {@link Perfume}s. Loads Perfume definitions that are
 * provided as JSONs under the {@link #STANDARD_PERFUME_DEFINITIONS_PACKAGE} under the resource directory.<br/>
 * Object mapping is done with the help of the <i>Jackson</i> library. For scanning the package for the
 * JSONs, the <i>ClassGraph</i> library is used (required to also detect resources when packaged as JAR application).
 */
@Slf4j
public class PerfumeRegistry implements DetectableRegistry<Perfume> {

    /**
     * Standard package/directory under the resources directory (e.g. src/main/resources or src/text/resources),
     * where Perfume definitions are located.
     */
    public static final String STANDARD_PERFUME_DEFINITIONS_PACKAGE = "de/jsilbereisen/perfumator/data/perfumes";

    /**
     * Standard package where all Perfume detectors are located.
     */
    public static final String STANDARD_PERFUME_DETECTORS_PACKAGE = "de.jsilbereisen.perfumator.engine.detector.perfume";

    private final String perfumePackage;

    private final String perfumeDetectorsPackage;

    private final String i18nPackage;

    private final String i18nPerfumesPackage;

    private final Map<Perfume, Detector<Perfume>> registry;

    private final  JsonMapper jsonMapper;

    public PerfumeRegistry() {
        perfumePackage = STANDARD_PERFUME_DEFINITIONS_PACKAGE;
        perfumeDetectorsPackage = STANDARD_PERFUME_DETECTORS_PACKAGE;

        i18nPackage = BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE;
        i18nPerfumesPackage = BundlesLoader.STANDARD_PERFUMES_PACKAGE;

        registry = new HashMap<>();
        jsonMapper = new JsonMapper();
    }

    public PerfumeRegistry(@Nullable String perfumePackage, @Nullable String perfumeDetectorsPackage,
                           @Nullable String i18nPackage, @Nullable String i18nPerfumesPackage) {
        this.perfumePackage = perfumePackage != null ? perfumePackage : STANDARD_PERFUME_DEFINITIONS_PACKAGE;
        this.perfumeDetectorsPackage = perfumeDetectorsPackage != null ? perfumeDetectorsPackage : STANDARD_PERFUME_DETECTORS_PACKAGE;

        this.i18nPackage = i18nPackage != null ? i18nPackage : BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE;
        this.i18nPerfumesPackage = i18nPerfumesPackage != null ? i18nPerfumesPackage :
                BundlesLoader.STANDARD_PERFUMES_PACKAGE;

        registry = new HashMap<>();
        jsonMapper = new JsonMapper();
    }

    // TODO: i18n exception messages
    /**
     * Detects and loads all Perfumes that are in the {@link #perfumePackage} (non-recursive) from their JSONs into the
     * registry and links them with their respective {@link Detector<Perfume>}.
     * Also performs internationalization for each loaded {@link Perfume} with the given {@link Locale},
     * if there are resources available.
     *
     * @param locale The locale to use for loading internationalized versions of the Perfumes.
     * @throws PerfumeLoadException If any failures occur within the detection and loading process.
     * @throws DetectorLoadException When being unable to instantiate the {@link Detector<Perfume>} for a
     *                               {@link Perfume} or when simply no {@link Detector} is found for it.
     */
    @Override
    public void loadRegistry(@NotNull Locale locale) {
        Bundles bundles = new Bundles();
        BundlesLoader bundlesLoader = new BundlesLoader(i18nPackage, i18nPerfumesPackage);
        bundlesLoader.loadDetectableBundles(bundles, locale);
        bundlesLoader.loadApplicationBundle(bundles, locale);

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
     * Scans the {@link #perfumePackage} non-recursively for all JSONs and tries to load a {@link Perfume}
     * instance for each of those, with the help of the <i>Jackson</i> object mapper.
     *
     * @param bundles Resources for internationalized exception messages.
     * @return The list of loaded Perfumes. Never {@code null}, might be empty.
     * @throws PerfumeLoadException If anything goes wrong in the process.
     */
    private @NotNull List<Perfume> loadPerfumes(@NotNull Bundles bundles) {
        List<Perfume> loadedPerfumes = new ArrayList<>();
        ClassGraph resourceScanner = new ClassGraph().acceptPathsNonRecursive(perfumePackage);

        try (ScanResult result = resourceScanner.scan()) {
            ResourceList allPerfumes = result.getAllResources();
            allPerfumes.stream()
                    .filter(resource -> resource.getPath().endsWith(".json"))
                    .forEach(resource -> {
                        Perfume perfume = loadSinglePerfume(bundles, resource);

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

    /**
     * Loads a single {@link Perfume} from its string-representation in JSON format with the <i>Jackson</i>
     * mapping library's API.
     *
     * @param bundles Resources for internationalized exception messages.
     * @param resource Resource with the {@link Perfume}'s JSON representation.
     * @return The loaded {@link Perfume} or {@code null} if some unknown failure, that does not trigger an
     *         exception, occurred
     * @throws PerfumeLoadException If the given resource could not be read or if the mapping of the resource's
     *                              content to a {@link Perfume} instance failed notably.
     */
    private @Nullable Perfume loadSinglePerfume(@NotNull Bundles bundles, @NotNull Resource resource) {
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
     * {@link Perfume#getDetectorClassSimpleName()}, with the {@link #STANDARD_PERFUME_DETECTORS_PACKAGE} prepended.
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
                detectorClass = Class.forName(perfumeDetectorsPackage + "."
                        + perfume.getDetectorClassSimpleName()).asSubclass(Detector.class);
            } catch (ClassNotFoundException e) {
                throw new DetectorLoadException("Unable to find detector class for Perfume \""
                        + perfume.getName() + "\" with class name \"" + perfume.getDetectorClassSimpleName() + "\"",
                        e);
            }

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

            detector.setConcreteDetectable(perfume);
            registry.put(perfume, detector);
        }
    }

    /**
     * Returns the registered {@link Perfume}s, sorted by their name.
     */
    @Override
    public @NotNull Set<Perfume> getRegisteredDetectables() {
        return new HashSet<>(registry.keySet());
    }

    /**
     * Returns all registered detectors.
     */
    @Override
    public @NotNull Set<Detector<Perfume>> getRegisteredDetectors() {
        return new HashSet<>(registry.values());
    }

    @Override
    public @Nullable Detector<Perfume> getDetector(@NotNull Perfume detectable) {
        return registry.get(detectable);
    }
}
