package de.jsilbereisen.perfumator.i18n;

import de.jsilbereisen.perfumator.io.LocaleOptionHandler;
import de.jsilbereisen.perfumator.util.StringUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Loads all resource bundles for the application and for perfumes, if such resources exist.
 * The loaded resources are stored in the {@link Bundles} class.
 */
@Slf4j
public class BundlesLoader {

    /**
     * Standard package for internationalization resources, relative to the resources folder (e.g.
     * src/main/resources or src/test/resources).
     */
    public static final String INTERNATIONALIZATION_PACKAGE = "de/jsilbereisen/perfumator/i18n";

    /**
     * Standard package of the bundle with application resources, relative to the resources folder (e.g.
     * src/main/resources or src/test/resources).
     */
    public static final String APPLICATION_PACKAGE = "application";

    /**
     * The package where the resource bundles for perfumes are stored, relative to the resources folder (e.g.
     * src/main/resources or src/test/resources).
     */
    public static final String PERFUMES_PACKAGE = "perfumes";

    /**
     * Name for the application base bundle. Contains resources that are not related to
     * specific perfumes.
     */
    public static final String APPLICATION_BASE_BUNDLE_NAME = "application";

    /**
     * Name for the command line base bundle. Contains resources for printing the usage text
     * on the command line.
     */
    public static final String CLI_BASE_BUNDLE_NAME = "commandline";

    /**
     * Pattern to match a resource bundle file with a locale-ending.
     */
    public static final Pattern LOCALE_ENDING_PATTERN = Pattern.compile("_[a-z]{2}$");

    private BundlesLoader() { }

    /**
     * Loads the resource bundles for the application for the given {@link Locale}.
     * If the given locale is {@code null}, the default is set to
     * {@link LocaleOptionHandler#getDefault()} and the fallback-files
     * should be loaded.
     *
     * @param locale The locale for which the resources should be loaded.
     */
    public static void loadApplicationBundle(@Nullable Locale locale) {
        Locale useLocale = locale != null ? locale : LocaleOptionHandler.getDefault();

        String applicationBundleFullQualified = StringUtil.joinStrings(
                List.of(INTERNATIONALIZATION_PACKAGE, APPLICATION_PACKAGE, APPLICATION_BASE_BUNDLE_NAME), "/");
        ResourceBundle applicationBundle =
                ResourceBundle.getBundle(applicationBundleFullQualified, useLocale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));


        Bundles.addBundle(applicationBundle);
    }

    /**
     * Loads the resource bundles for the Code Perfumes for the given {@link Locale}.
     * If the given locale is {@code null}, the default is set to
     * {@link LocaleOptionHandler#getDefault()} and the fallback-files
     * are loaded.
     *
     * @param locale The locale for which the resources should be loaded.
     */
    public static void loadPerfumeBundles(@Nullable Locale locale) {
        Locale useLocale = locale != null ? locale : LocaleOptionHandler.getDefault();

        ClassGraph resourceScanner = new ClassGraph().acceptPathsNonRecursive(INTERNATIONALIZATION_PACKAGE +
                "/perfumes");
        Set<String> detectedBaseBundles = new HashSet<>();

        try (ScanResult result = resourceScanner.scan()) {
            ResourceList all = result.getAllResources();
            all.stream().map(Resource::getPath)
                    .filter(resourcePath -> resourcePath.endsWith(".properties"))
                    .forEach(resourcePath -> {
                        int lastPathDelimiter = resourcePath.lastIndexOf("/");
                        String fileNameWithExt = resourcePath.substring(lastPathDelimiter + 1);

                        String fileName = fileNameWithExt.substring(0,
                                fileNameWithExt.length() - ".properties".length());

                        if (LOCALE_ENDING_PATTERN.matcher(fileName).find()) {
                            fileName = fileName.substring(0, fileName.length() - 3);
                        }

                        detectedBaseBundles.add(fileName);
                    });
        } catch (Exception e) {
            log.error("Could not load Perfume resource bundles.");
        }

        Bundles.DETECTED_PERFUME_BUNDLES.clear();

        detectedBaseBundles.forEach(detectedBaseBundle -> {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    StringUtil.joinStrings(List.of(INTERNATIONALIZATION_PACKAGE, PERFUMES_PACKAGE,
                            detectedBaseBundle), "/"),
                    useLocale,
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

            Bundles.addBundle(bundle);
            Bundles.DETECTED_PERFUME_BUNDLES.add(detectedBaseBundle);
        });
    }

    /**
     * Loads the resources related to the command line inputs and outputs.
     * If the given locale is {@code null}, the default is set to
     * {@link LocaleOptionHandler#getDefault()} and the fallback-files
     * are loaded.
     */
    public static void loadCliBundle(@Nullable Locale locale) {
        Locale useLocale = locale != null ? locale : LocaleOptionHandler.getDefault();

        ResourceBundle cliBundle = ResourceBundle.getBundle(
                StringUtil.joinStrings(List.of(INTERNATIONALIZATION_PACKAGE, APPLICATION_PACKAGE,
                        CLI_BASE_BUNDLE_NAME), "/"),
                useLocale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

        Bundles.setCliBundle(cliBundle);
    }
}
