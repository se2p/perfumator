package de.jsilbereisen.perfumator.model;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import de.jsilbereisen.perfumator.io.LanguageTag;
import de.jsilbereisen.perfumator.io.output.OutputFormat;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Configuration data for the analysis engine. Instances of this class are immutable.
 */
@Value
public class EngineConfiguration {

    Path sourcesPath;

    Path outputDir;

    Locale resourcesLocale;

    OutputFormat outputFormat;

    /**
     * Constructor with must-have parameters. The {@link Locale} to use for resource loading
     * will be set to {@link LanguageTag#getDefault()} and the {@link OutputFormat} will be set to
     * {@link OutputFormat#getDefault()}.
     *
     * @param sourcesPath Path to directory with Java Sources to analyze, or to a single Java source file.
     * @param outputDir   Path where the output should be generated. Has to be a directory.
     */
    public EngineConfiguration(@NotNull Path sourcesPath, @NotNull Path outputDir) {
        this.sourcesPath = sourcesPath;
        this.outputDir = outputDir;
        this.resourcesLocale = LanguageTag.getDefault().getRelatedLocale();
        this.outputFormat = OutputFormat.getDefault();
    }

    /**
     * Constructor with all possible configuration fields.
     *
     * @param sourcesPath     Path to directory with Java Sources to analyze, or to a single Java source file.
     * @param outputDir       Path where the output should be generated. Has to be a directory.
     * @param resourcesLocale The locale the engine should use when resources are loaded.
     * @param outputFormat    The format for the generated output.
     */
    public EngineConfiguration(@NotNull Path sourcesPath, @NotNull Path outputDir,
                               @NotNull Locale resourcesLocale, @NotNull OutputFormat outputFormat) {
        this.sourcesPath = sourcesPath;
        this.outputDir = outputDir;
        this.resourcesLocale = resourcesLocale;
        this.outputFormat = outputFormat;
    }
}
