package de.jsilbereisen.perfumator.io.output.json;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.io.output.AbstractOutputGenerator;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.io.output.OutputGenerator;
import de.jsilbereisen.perfumator.model.Detectable;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Abstract class for a {@link OutputGenerator} that produces output in JSON Format.
 * For (de-)serializing, the <i>FasterXML Jackson Object Mapper</i> is used.
 *
 * @param <T> The {@link Detectable} that the output is produced for.
 */
public abstract class JsonOutputGenerator<T extends Detectable> extends AbstractOutputGenerator<T> {

    /**
     * File name pattern WITH file extension for JSON listings of {@link DetectedInstance}s.
     */
    public static final Pattern LISTINGS_FILE_PATTERN = Pattern.compile(OutputGenerator.DETECTIONS_FILES_NAME_PATTERN
            + OutputFormat.JSON.getFileExtension() + "$");

    /**
     * Instance of the Object mapper.
     */
    protected final JsonMapper mapper;

    protected JsonOutputGenerator(@NotNull OutputConfiguration config, @NotNull DetectableRegistry<T> registry,
                                  @Nullable Bundles bundles) {
        super(config, registry, bundles, OutputFormat.JSON);

        mapper = new JsonMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
}
