package de.jsilbereisen.perfumator.i18n;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loads all resource bundles for the application and for perfumes, if such resources exist.
 * The loaded resources are stored in the {@link Bundles} class.
 */
@Slf4j
public class BundlesLoader {

    /**
     * The standard path for the application's resources, relative to the project root.
     */
    public static final Path STANDARD_RESOURCES_DIRECTORY = Paths.get("src", "main", "resources");

    /**
     * The path where the resource bundles for perfumes are stored, relative to the resources folder (p.e.
     * src/main/resources or src/test/resources).
     */
    public static final Path PATH_TO_PERFUME_BUNDLES_DIR = Paths.get("i18n", "perfumes");

    /**
     * Standard name for the application base bundle. Contains resources that are not related to
     * specific perfumes.
     */
    public static final String APPLICATION_BASE_BUNDLE_NAME = "application";

    /**
     * Standard path to the bundle with non-perfume-related resources, relative to the resources folder (p.e.
     * src/main/resources or src/test/resources).
     */
    public static final Path PATH_TO_APPLICATION_BUNDLE = Paths.get("i18n", APPLICATION_BASE_BUNDLE_NAME, APPLICATION_BASE_BUNDLE_NAME);

    /**
     * Pattern to match a resource bundle file with a locale-ending.
     */
    public static final Pattern LOCALE_ENDING_PATTERN = Pattern.compile("_[a-z]{2}_[A-Z]{2}$");

    private BundlesLoader() { }

    /**
     * Loads the resource bundles for the application and the Code Perfumes for the given {@link Locale}.
     * If the given locale is {@code null}, the default is set to {@link Locale#ENGLISH} and the fallback-files
     * should be loaded.
     *
     * @param locale The locale for which the resources should be loaded.
     * @param resourcesPath Path to the applications resources folder (e.g. src/main/resources).
     */
    public static void loadBundles(@Nullable Locale locale, @NotNull Path resourcesPath) throws IOException {
        Locale useLocale = locale != null ? locale : Locale.ENGLISH;
        Locale.setDefault(Locale.ENGLISH);

        loadApplicationBundle(useLocale);
        loadPerfumeBundles(useLocale, resourcesPath);
    }

    private static void loadApplicationBundle(@NotNull Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(PATH_TO_APPLICATION_BUNDLE.toString(), locale);

        log.info("Loaded application resource bundle.");
        Bundles.addBundle(bundle);
    }

    private static void loadPerfumeBundles(@NotNull Locale locale, @NotNull Path resourcesPath) throws IOException {
        Set<String> detectedBaseBundles = new HashSet<>();

        try (Stream<Path> pathsInDir = Files.list(resourcesPath.resolve(PATH_TO_PERFUME_BUNDLES_DIR))) {
            pathsInDir.filter(path -> !Files.isDirectory(path))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".properties"))
                    .forEach(fileName -> {
                        String baseBundleName = fileName.substring(0, fileName.length() - ".properties".length());

                        if (LOCALE_ENDING_PATTERN.matcher(baseBundleName).find()) {
                            baseBundleName = baseBundleName.substring(0, baseBundleName.length() - 6);
                        }

                        detectedBaseBundles.add(baseBundleName);
                    });
        }

        detectedBaseBundles.forEach(detectedBaseBundle -> {
            ResourceBundle bundle = ResourceBundle.getBundle(PATH_TO_PERFUME_BUNDLES_DIR
                    .resolve(detectedBaseBundle).toString(), locale);

            log.info("Loaded bundle with base name \"" + detectedBaseBundle + "\"");
            Bundles.addBundle(bundle);
        });
    }
}
