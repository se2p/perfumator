package test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.io.output.json.JsonOutputGenerator;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import de.jsilbereisen.perfumator.util.JsonDeserializationUtil;
import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractJsonOutputTest extends AbstractOutputTest {

    protected static final Pattern LISTINGS_FILE_PATTERN = JsonOutputGenerator.LISTINGS_FILE_PATTERN;

    protected static final JsonMapper MAPPER = new JsonMapper();
    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    protected static <T extends Detectable> @NotNull List<DetectedInstance<T>> readList(
            @NotNull TypeReference<List<DetectedInstance<T>>> typeReference, @NotNull Path path) {
        try {
            return JsonDeserializationUtil.readList(MAPPER, typeReference, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Detectable> @NotNull DetectedInstance<T> readSingle(
            @NotNull TypeReference<DetectedInstance<T>> typeReference, @NotNull Path path) {
        try {
            return JsonDeserializationUtil.readSingle(MAPPER, typeReference, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends Detectable> @NotNull StatisticsSummary<T> readStatistics(
            @NotNull TypeReference<StatisticsSummary<T>> typeReference, @NotNull Path path,
            @NotNull DetectableRegistry<T> registry, @NotNull Class<T> forType) {
        SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(forType, new JsonDeserializationUtil.StatisticsSummaryDeserializer<>(registry));
        MAPPER.registerModule(module);

        try {
            return JsonDeserializationUtil.readStatistics(MAPPER, typeReference, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
