package de.jsilbereisen.perfumator.i18n;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Class that holds a map with all known resources from {@link ResourceBundle}s that are loaded by the
 * {@link BundlesLoader}. For internationalization purposes.
 */
public class Bundles {

    private final Map<String, String> resources = new HashMap<>();

    /**
     * Needed for command line internationalization with the <b>Args4j</b> library.
     */
    @Getter(onMethod = @__({@Nullable}))
    @Setter
    private ResourceBundle cliBundle;

    /**
     * Adds all contents of a resource bundle to the map of known resources.
     * Prefixes all found keys in the bundle with the name of the base bundle,
     * to avoid bugs through duplicates. As the application language is fixed at runtime,
     * there is no need to store multiple language versions of one key.<p/>
     * Example:<br/>
     * The value of key <i>hello.world</i> in the bundle <i>some_bundle_de_DE</i>
     * is added with the key <i>some_bundle.hello.world</i>.
     */
    public void addBundle(@NotNull ResourceBundle bundle) {
        String baseBundle = bundle.getBaseBundleName();
        String baseBundleWithoutPath = Paths.get(baseBundle).getFileName().toString();

        for (String key : bundle.keySet()) {
            resources.put(baseBundleWithoutPath + "." + key, bundle.getString(key));
        }
    }

    /**
     * Returns the resource that is stored for the given key.
     * The returned value might be {@code null} if no resource is found.
     */
    public @Nullable String getResource(@NotNull String key) {
        return resources.get(key);
    }

    /**
     * Returns the resource that is stored for the given key with the name of the given
     * class as a prefix.
     * Return value might be {@code null} if no resource is found.
     */
    public @Nullable String getResource(@NotNull String key, @NotNull Class<?> clazz) {
        return resources.get(clazz.getSimpleName() + "." + key);
    }

    /**
     * Returns the resource that is stored for the given key with the prefix "application.".
     * The returned value might be {@code null} if no resource is found.
     */
    public @Nullable String getApplicationResource(@NotNull String key) {
        return resources.get(BundlesLoader.APPLICATION_BASE_BUNDLE_NAME + "." + key);
    }
}
