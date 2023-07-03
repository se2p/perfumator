package de.jsilbereisen.perfumator.io.output;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.model.Detectable;

/**
 * Abstract class for an output-generator.
 * Has an {@link OutputConfiguration} and a {@link DetectableRegistry} which holds the interesting
 * {@link Detectable}s that were analysed.
 * <br/>
 * Has methods to generate a statistical overview over the detections of {@link T} and to generate a listing of all
 * concrete detections of {@link T}.
 *
 * @param <T> The type of {@link Detectable} for which this {@link AbstractOutputGenerator}
 *            can generate output.
 */
public abstract class AbstractOutputGenerator<T extends Detectable> implements OutputGenerator<T> {

    @NotNull
    protected final OutputConfiguration config;

    @NotNull
    protected final Bundles i18nResources;

    @NotNull
    protected final OutputFormat outputFormat;

    /**
     * Depending on the {@link OutputConfiguration},
     * generates a listing of all the detections, or only adds them to the overall statistics.
     */
    protected AbstractOutputGenerator(@NotNull OutputConfiguration config, @Nullable Bundles i18nResources,
                                      @NotNull OutputFormat outputFormat) {
        this.config = config;
        this.i18nResources = i18nResources == null ? new Bundles() : i18nResources;
        this.outputFormat = outputFormat;
    }

    @Override
    public @NotNull OutputFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public @NotNull OutputConfiguration getConfig() {
        return config;
    }
}
