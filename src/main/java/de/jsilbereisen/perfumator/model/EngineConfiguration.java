package de.jsilbereisen.perfumator.model;

import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import de.jsilbereisen.perfumator.io.LanguageTag;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.*;

/**
 * Configuration DTO for the analysis engine, usually stems from commandline input.
 */
@Getter
@EqualsAndHashCode
public class EngineConfiguration {

    private final Path sourcesPath;

    private final Path outputDir;

    private final Locale resourcesLocale;

    private final OutputFormat outputFormat;

    private final int batchSize;

    @Unmodifiable
    private final List<Path> dependencies;

    private EngineConfiguration(@NotNull Builder builder) {
        this.sourcesPath = builder.sourcesPath;
        this.outputDir = builder.outputDir;
        this.resourcesLocale = builder.resourcesLocale;
        this.outputFormat = builder.outputFormat;
        this.batchSize = builder.batchSize;
        this.dependencies = Collections.unmodifiableList(builder.dependencies);
    }

    public static Builder builder(@NotNull Path sourcesPath, @NotNull Path outputDir) {
        return new Builder(sourcesPath, outputDir);
    }

    public static class Builder {

        private final Path sourcesPath;

        private final Path outputDir;

        private Locale resourcesLocale = LanguageTag.getDefault().getRelatedLocale();

        private OutputFormat outputFormat = OutputFormat.getDefault();

        private int batchSize = OutputConfiguration.DEFAULT_BATCH_SIZE;

        @NotNull
        private List<Path> dependencies = new ArrayList<>();

        public Builder(@NotNull Path sourcesPath, @NotNull Path outputDir) {
            this.sourcesPath = sourcesPath;
            this.outputDir = outputDir;
        }

        public Builder resourcesLocale(@NotNull Locale locale) {
            this.resourcesLocale = locale;
            return this;
        }

        public Builder resourcesLocale(@NotNull LanguageTag languageTag) {
            this.resourcesLocale = languageTag.getRelatedLocale();
            return this;
        }

        public Builder outputFormat(@NotNull OutputFormat format) {
            this.outputFormat = format;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder setDependencies(@NotNull Collection<Path> dependencies) {
            this.dependencies = new ArrayList<>(dependencies);
            return this;
        }

        public Builder clearDependencies() {
            this.dependencies = new ArrayList<>();
            return this;
        }

        public Builder addDependency(@NotNull Path dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        @NotNull
        public EngineConfiguration build() {
            return new EngineConfiguration(this);
        }
    }
}
