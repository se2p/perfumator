package de.jsilbereisen.perfumator.util;

import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.StatisticsSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JsonDeserializationUtil {

    private JsonDeserializationUtil() {
    }

    @NotNull
    public static <T extends Detectable> List<DetectedInstance<T>> readList(
            @NotNull JsonMapper mapper, @NotNull TypeReference<List<DetectedInstance<T>>> typeReference,
            @NotNull Path path) throws IOException {
        return read(mapper, typeReference, path);
    }

    @NotNull
    public static <T extends Detectable> DetectedInstance<T> readSingle(
            @NotNull JsonMapper mapper, @NotNull TypeReference<DetectedInstance<T>> typeReference,
            @NotNull Path path) throws IOException {
        return read(mapper, typeReference, path);
    }

    @NotNull
    public static <T extends Detectable> StatisticsSummary<T> readStatistics(
            @NotNull JsonMapper mapper, @NotNull TypeReference<StatisticsSummary<T>> typeReference,
            @NotNull Path path) throws IOException {
        if (!Files.isRegularFile(path) && !path.toString().endsWith(OutputFormat.JSON.getFileExtension())) {
            throw new IllegalArgumentException("Path must point to an existing " + OutputFormat.JSON.getAbbreviation()
                    + " file.");
        }

        return read(mapper, typeReference, path);
    }

    @NotNull
    private static <T> T read(@NotNull JsonMapper mapper, @NotNull TypeReference<T> typeReference,
                              @NotNull Path path) throws IOException {
        if (!Files.isRegularFile(path) && !path.toString().endsWith(OutputFormat.JSON.getFileExtension())) {
            throw new IllegalArgumentException("Path must point to an existing " + OutputFormat.JSON.getAbbreviation()
                    + " file.");
        }

        return mapper.readValue(path.toFile(), typeReference);
    }

    /**
     * Deserializer that is required to be registered to the {@link ObjectMapper} in use,
     * when deserializing a {@link StatisticsSummary}.
     *
     * @param <T> The type of {@link Detectable}.
     */
    public static class StatisticsSummaryDeserializer<T extends Detectable> extends KeyDeserializer {

        private final DetectableRegistry<T> registry;

        public StatisticsSummaryDeserializer(@NotNull DetectableRegistry<T> registry) {
            this.registry = registry;
        }

        /**
         * Key-Deserializer for the Map that is used in {@link StatisticsSummary}.
         * As {@link Detectable} declares the "name" property as the {@link JsonKey},
         * the read {@link String} key is being mapped to a {@link T} of the registry
         * by the "name" property, or to {@code null} if the registry has no fitting {@link T}
         * registered.
         */
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            Optional<T> mapped = registry.getRegisteredDetectables().stream().filter(det -> key.equals(det.getName()))
                    .findFirst();

            if (mapped.isEmpty()) {
                throw new IllegalStateException("The key \"" + key + "\" (= name of a Detectable)"
                        + " could not be matched to any registered Detectable.");
            }

            return mapped.get();
        }
    }
}
