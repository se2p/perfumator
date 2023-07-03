package de.jsilbereisen.perfumator.io.output.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.DetectedInstanceComparator;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.JsonDeserializationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class PerfumeJsonOutputGenerator extends JsonOutputGenerator<Perfume> {

    private final StatisticsSummary<Perfume> summary;
    private int lastListingNumber = 0;

    public PerfumeJsonOutputGenerator(@NotNull OutputConfiguration config,
                                      @NotNull DetectableRegistry<Perfume> registry,
                                      @Nullable Bundles bundles) {
        super(config, registry, bundles);

        SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(Perfume.class,
                new JsonDeserializationUtil.StatisticsSummaryDeserializer<>(registry));
        mapper.registerModule(module);

        summary = StatisticsSummary.from(registry);
    }

    @Override
    public void handle(@NotNull List<DetectedInstance<Perfume>> detectedInstances) throws IOException {
        if (!Files.isDirectory(config.getOutputDirectory())) {
            throw new IllegalStateException("Mal-configured instance. Output path must be an existing directory!");
        }

        summary.addToStatistics(detectedInstances);

        if (lastListingNumber == 1) {
            renameLonelyListing();
        }

        if (lastListingNumber > 0) {
            List<DetectedInstance<Perfume>> deserialized = readAndDeleteIfNotFull(lastListingNumber);
            if (!deserialized.isEmpty()) {
                detectedInstances.addAll(deserialized);
                lastListingNumber--;
            }
        }

        detectedInstances.sort(new DetectedInstanceComparator<>());

        int numberOfOutputFilesToCreate = detectedInstances.size() / config.getBatchSize();
        numberOfOutputFilesToCreate += detectedInstances.size() % config.getBatchSize() > 0 ? 1 : 0;

        if (numberOfOutputFilesToCreate == 1) {
            createSingleListing(detectedInstances, lastListingNumber++);
        } else {
            createMultipleListings(detectedInstances, numberOfOutputFilesToCreate, lastListingNumber + 1);
            lastListingNumber += numberOfOutputFilesToCreate;
        }
    }

    @Override
    public void complete() throws IOException {
        // TODO: In the statistic, only files where an actual detection happened are captured! Need to expand engine
        //  to capture a list of all files?

        Path summaryFileName = Path.of(SUMMARY_FILE_SUFFIX + outputFormat.getFileExtension());
        Path toCreate = config.getOutputDirectory().resolve(summaryFileName);

        Files.deleteIfExists(toCreate);
        Path created = Files.createFile(toCreate);

        mapper.writeValue(created.toFile(), summary);
    }

    @NotNull
    private List<DetectedInstance<Perfume>> readAndDeleteIfNotFull(int listingNumber) throws IOException {
        Path file = config.getOutputDirectory().resolve(listingFileName(listingNumber));
        List<DetectedInstance<Perfume>> deserialized = JsonDeserializationUtil.readList(mapper, new TypeReference<>() {
        }, file);

        if (deserialized.isEmpty() || deserialized.size() == config.getBatchSize()) {
            return Collections.emptyList();
        } else {
            Files.deleteIfExists(file);
            return deserialized;
        }
    }

    private void createSingleListing(@NotNull List<DetectedInstance<Perfume>> detectedInstances,
                                     int listingNumber) throws IOException {
        Path newFileName = listingFileName(listingNumber);

        Path toCreate = config.getOutputDirectory().resolve(newFileName);
        Path created = Files.createFile(toCreate);

        mapper.writeValue(created.toFile(), detectedInstances);
    }

    private void createMultipleListings(List<DetectedInstance<Perfume>> detectedInstances, int numberOfOutputFiles,
                                        int firstNewListingNumber) throws IOException {
        for (int i = firstNewListingNumber; i < numberOfOutputFiles + firstNewListingNumber; i++) {
            Path toCreate = config.getOutputDirectory().resolve(listingFileName(i));
            Path created = Files.createFile(toCreate);

            int low = (i - 1) * config.getBatchSize();
            int high = Math.min(i * config.getBatchSize(), detectedInstances.size());
            mapper.writeValue(created.toFile(), detectedInstances.subList(low, high));
        }
    }

    private void renameLonelyListing() throws IOException {
        Path lonelyListing
                = config.getOutputDirectory().resolve(listingFileName(0));

        Files.move(lonelyListing, lonelyListing.resolveSibling(listingFileName(1)));
    }

    @NotNull
    private Path listingFileName(int fileNum) {
        String numberSuffix = fileNum != 0 ? SUFFIX_CONNECTOR + fileNum : "";
        return Path.of(DETECTIONS_FILE_SUFFIX + numberSuffix + outputFormat.getFileExtension());
    }
}
