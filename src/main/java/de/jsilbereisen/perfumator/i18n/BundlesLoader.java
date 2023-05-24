package de.jsilbereisen.perfumator.i18n;

import de.jsilbereisen.perfumator.io.LocaleOptionHandler;
import de.jsilbereisen.perfumator.util.StringUtil;
import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Provides methods to load resource bundles for the application and other auto-detectable resources from a
 * specified package, usually for {@link de.jsilbereisen.perfumator.model.Detectable}s, if such resources exist.
 */
@Slf4j
public class BundlesLoader {

    /**
     * Standard package for internationalization resources, relative to the resources folder (e.g.
     * src/main/resources or src/test/resources).
     */
    public static final String STANDARD_INTERNATIONALIZATION_PACKAGE = "de/jsilbereisen/perfumator/i18n";

    /**
     * Standard package of the bundle with application resources, relative to the overall internationalization
     * resources (e.g. {@link #STANDARD_INTERNATIONALIZATION_PACKAGE}).
     */
    public static final String STANDARD_APPLICATION_PACKAGE = "application";

    /**
     * Standard package of the bundles with {@link de.jsilbereisen.perfumator.model.Perfume} related
     * internationalization resources, relative to the overall internationalization resources (e.g.
     * {@link #STANDARD_INTERNATIONALIZATION_PACKAGE}).
     */
    public static final String STANDARD_PERFUMES_PACKAGE = "perfumes";

    /**
     * Name for the application base bundle.
     */
    public static final String APPLICATION_BASE_BUNDLE_NAME = "application";

    /**
     * Name for the command line base bundle. Contains resources for printing the usage text
     * and other messages that result from the application start via the command line interface.
     */
    public static final String CLI_BASE_BUNDLE_NAME = "commandline";

    /**
     * Pattern to match a resource bundle file with a locale-ending.
     */
    public static final Pattern LOCALE_ENDING_PATTERN = Pattern.compile("_[a-z]{2}$");

    private final String i18nPackage;

    private final String detectableResourcesPackage;

    private final String applicationResourcesPackage;

    /**
     * <p>
     * Constructor that sets the packages where the internationalization resources that this {@link BundlesLoader}
     * instance can load are located, as well as the sub-package of it where the resources (usually for
     * {@link de.jsilbereisen.perfumator.model.Detectable}s) that should be auto-detected are.
     * The sub-package for the application resources is set to {@link #STANDARD_APPLICATION_PACKAGE}.
     * </p>
     * <p>
     * <b>CAUTION:</b><br/>
     * The packages have to be given in the form "x/y/z" instead of "x.y.z".
     * There are <b>NO CHECKS</b> in the constructor whether the given packages are valid or even exist!
     * </p>
     *
     * @param i18nPackagePath Root package where the resources are located.
     * @param detectableResourcesPackagePath Sub-package of the first parameter, where resources that should
     *                                       be auto-detected are located.
     * @throws IllegalArgumentException If any given package string is {@code null} or empty.
     */
    public BundlesLoader(@NotNull String i18nPackagePath, @NotNull String detectableResourcesPackagePath) {
        if (StringUtil.anyEmpty(i18nPackagePath, detectableResourcesPackagePath)) {
            throw new IllegalArgumentException("Given package paths must neither be null nor empty.");
        }

        this.i18nPackage = i18nPackagePath;
        this.detectableResourcesPackage = detectableResourcesPackagePath;
        this.applicationResourcesPackage = STANDARD_APPLICATION_PACKAGE;
    }

    /**
     * <p>
     * Constructor that sets the packages where the internationalization resources that this {@link BundlesLoader}
     * instance can load are located, as well as the sub-package of it where the resources (usually for
     * {@link de.jsilbereisen.perfumator.model.Detectable}s) that should be auto-detected are.
     * The third parameter sets the sub-package for the application resources.
     * </p>
     * <p>
     * <b>CAUTION:</b><br/>
     * The packages have to be given in the form "x/y/z" instead of "x.y.z".
     * There are <b>NO CHECKS</b> in the constructor whether the given packages are valid or even exist!
     * </p>
     *
     * @param i18nPackagePath Root package where the resources are located.
     * @param detectableResourcesPackagePath Sub-package of the first parameter, where resources that should
     *                                       be auto-detected are located.
     * @throws IllegalArgumentException If any given package string is {@code null} or empty.
     */
    public BundlesLoader(@NotNull String i18nPackagePath, @NotNull String detectableResourcesPackagePath,
                         @NotNull String applicationResourcesPackagePath) {
        if (StringUtil.anyEmpty(i18nPackagePath, detectableResourcesPackagePath)) {
            throw new IllegalArgumentException("Given package paths must neither be null nor empty.");
        }

        this.i18nPackage = i18nPackagePath;
        this.detectableResourcesPackage = detectableResourcesPackagePath;
        this.applicationResourcesPackage = applicationResourcesPackagePath;
    }

    /**
     * Loads the resource bundles for the application for the given {@link Locale}.
     * If the given locale is {@code null}, the default is set to
     * {@link LocaleOptionHandler#getDefault()} and the fallback-files
     * should be loaded.
     *
     * @param locale The locale for which the resources should be loaded.
     */
    public void loadApplicationBundle(@NotNull Bundles bundlesHolder, @Nullable Locale locale) {
        Locale useLocale = locale != null ? locale : LocaleOptionHandler.getDefault();

        String applicationBundleFullQualified = StringUtil.joinStrings(
                List.of(i18nPackage, applicationResourcesPackage, APPLICATION_BASE_BUNDLE_NAME), "/");
        ResourceBundle applicationBundle =
                ResourceBundle.getBundle(applicationBundleFullQualified, useLocale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));


        bundlesHolder.addBundle(applicationBundle);
    }

    /**
     * Loads the auto-detectable resource bundles under the configured package for the given {@link Locale}.
     * If the given locale is {@code null}, the default is set to {@link LocaleOptionHandler#getDefault()} and the
     * fallback-files are loaded.
     *
     * @param locale The locale for which the resources should be loaded.
     */
    public void loadDetectableBundles(@NotNull Bundles bundlesHolder, @Nullable Locale locale) {
        Locale useLocale = locale != null ? locale : LocaleOptionHandler.getDefault();

        ClassGraph resourceScanner = new ClassGraph().acceptPathsNonRecursive(i18nPackage +
                "/" + detectableResourcesPackage);
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
        } catch (ClassGraphException e) {
            // TODO: Define + throw own exception?
            log.error("Could not load Perfume resource bundles.");
            throw e;
        }

        detectedBaseBundles.forEach(detectedBaseBundle -> {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    StringUtil.joinStrings(List.of(i18nPackage, detectableResourcesPackage, detectedBaseBundle), "/"),
                    useLocale,
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

            bundlesHolder.addBundle(bundle);
        });
    }

    /**
     * Loads the resources related to the command line inputs and outputs.
     * If the given locale is {@code null}, the default is set to
     * {@link LocaleOptionHandler#getDefault()} and the fallback-files
     * are loaded.
     */
    public void loadCliBundle(@NotNull Bundles bundlesHolder, @Nullable Locale locale) {
        Locale useLocale = locale != null ? locale : LocaleOptionHandler.getDefault();

        ResourceBundle cliBundle = ResourceBundle.getBundle(
                StringUtil.joinStrings(List.of(i18nPackage, applicationResourcesPackage,
                        CLI_BASE_BUNDLE_NAME), "/"),
                useLocale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

        bundlesHolder.setCliBundle(cliBundle);
    }
}
