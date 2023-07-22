package io.output;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import test.AbstractJsonOutputTest;
import test.PerfumeTestUtil;

import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputGenerator;
import de.jsilbereisen.perfumator.io.output.json.PerfumeJsonOutputGenerator;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonOutputGeneratorTest extends AbstractJsonOutputTest {

    @Test
    void listingSerializationAndDeserialization() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR);
        DetectedInstance<Perfume> detectedInstance = PerfumeTestUtil.singleExampleDetectedInstance();
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        outputGenerator.handle(new ArrayList<>(List.of(detectedInstance)));

        Path pathToExpectedOutput = OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR.resolve("detections.json");
        assertThat(Files.exists(pathToExpectedOutput)).isTrue();

        List<DetectedInstance<Perfume>> output = readList(new TypeReference<>() {}, pathToExpectedOutput);
        assertThat(output).hasSize(1);

        DetectedInstance<Perfume> outputPerfume = output.get(0);
        DetectedInstance<Perfume> comparison = readSingle(new TypeReference<>() {},
                OUTPUT_TEST_COMPARISON_RESOURCES_ROOT_DIR.resolve("testListingOutputForSingleSourceFile.json"));

        assertThat(outputPerfume).isEqualTo(comparison);
    }

    @Test
    void listingOutputBatchIntoMultipleFiles() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR).setBatchSize(100);
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            detections.add(PerfumeTestUtil.singleExampleDetectedInstance().setTypeName(String.valueOf(i)));
        }

        outputGenerator.handle(detections);

        List<Path> outputFiles = getDetectionsOutputPaths(LISTINGS_FILE_PATTERN);
        assertThat(outputFiles).hasSize(10);

        TypeReference<List<DetectedInstance<Perfume>>> typeRef = new TypeReference<>() {
        };
        outputFiles.forEach(path -> {
            List<DetectedInstance<Perfume>> deserialized = readList(typeRef, path);

            assertThat(deserialized).hasSize(100);
        });
    }

    @Test
    void listingOutputBatchUnevenDivision() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR).setBatchSize(100);
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();
        for (int i = 0; i < 199; i++) {
            detections.add(PerfumeTestUtil.singleExampleDetectedInstance().setTypeName(String.valueOf(i)));
        }

        outputGenerator.handle(detections);

        List<Path> outputFiles = getDetectionsOutputPaths(LISTINGS_FILE_PATTERN);
        outputFiles.sort(Path::compareTo);
        assertThat(outputFiles).hasSize(2);

        TypeReference<List<DetectedInstance<Perfume>>> typeRef = new TypeReference<>() {
        };
        List<DetectedInstance<Perfume>> first = readList(typeRef, outputFiles.get(0));
        List<DetectedInstance<Perfume>> second = readList(typeRef, outputFiles.get(1));

        assertThat(first).hasSize(100);
        assertThat(second).hasSize(99);
    }

    @Test
    void listingOutputMaxBatchSize() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR).setBatchSize(OutputConfiguration.MAX_BATCH_SIZE);
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();
        for (int i = 0; i < config.getBatchSize(); i++) {
            detections.add(PerfumeTestUtil.singleExampleDetectedInstance().setTypeName(String.valueOf(i)));
        }

        outputGenerator.handle(detections);

        List<Path> outputFiles = getDetectionsOutputPaths(LISTINGS_FILE_PATTERN);
        outputFiles.sort(Path::compareTo);
        assertThat(outputFiles).hasSize(1);

        TypeReference<List<DetectedInstance<Perfume>>> typeRef = new TypeReference<>() {
        };
        List<DetectedInstance<Perfume>> deserialized = readList(typeRef, outputFiles.get(0));

        assertThat(deserialized).hasSize(OutputConfiguration.MAX_BATCH_SIZE);
    }

    @Test
    void listingNoOutput() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR);
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        outputGenerator.handle(detections);

        List<Path> outputFiles = getDetectionsOutputPaths(LISTINGS_FILE_PATTERN);
        assertThat(outputFiles).isEmpty();
    }

    @Test
    void multipleHandleCalls() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR)
                .setBatchSize(100);
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        List<DetectedInstance<Perfume>> detectionsFirstCall = new ArrayList<>();
        List<DetectedInstance<Perfume>> detectionsSecondCall = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            detectionsFirstCall.add(PerfumeTestUtil.singleExampleDetectedInstance().setTypeName(String.valueOf(i)));
            detectionsSecondCall.add(PerfumeTestUtil.singleExampleDetectedInstance().setTypeName(String.valueOf(-i)));
        }

        outputGenerator.handle(detectionsFirstCall);
        outputGenerator.handle(detectionsSecondCall);

        List<Path> outputFiles = getDetectionsOutputPaths(LISTINGS_FILE_PATTERN);

        outputFiles.sort(Path::compareTo);
        assertThat(outputFiles.get(0).toString()).endsWith("detections_1.json");
        assertThat(outputFiles.get(1).toString()).endsWith("detections_2.json");
        assertThat(outputFiles).hasSize(2);

        TypeReference<List<DetectedInstance<Perfume>>> typeRef = new TypeReference<>() {
        };
        List<DetectedInstance<Perfume>> deserialized = readList(typeRef, outputFiles.get(0));
        assertThat(deserialized).hasSize(100); // batch size

        deserialized.addAll(readList(typeRef, outputFiles.get(1)));
        assertThat(deserialized).hasSize(140);
    }

    @Test
    void generateStatistics() throws IOException {
        OutputConfiguration config = OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR);

        Perfume perf1 = PerfumeTestUtil.singleExamplePerfume();
        Perfume perf2 = PerfumeTestUtil.singleExamplePerfume();
        perf2.setName("Another");

        DetectableRegistry<Perfume> registry = PerfumeTestUtil.mockedRegistryWithPerfumes(perf1, perf2);
        OutputGenerator<Perfume> outputGenerator = new PerfumeJsonOutputGenerator(config, null);

        Path here = Path.of(".");
        DetectedInstance<Perfume> det1 = new DetectedInstance<Perfume>().setDetectable(perf1).setSourceFile(here)
                .setTypeName("OuterClass");
        DetectedInstance<Perfume> det2 = new DetectedInstance<Perfume>().setDetectable(perf1).setSourceFile(here)
                .setTypeName("InnerClass");

        DetectedInstance<Perfume> det3 = new DetectedInstance<Perfume>().setDetectable(perf2)
                .setSourceFile(Path.of("somewhere")).setTypeName("SomeClass");
        DetectedInstance<Perfume> det4 = new DetectedInstance<Perfume>().setDetectable(perf2)
                .setSourceFile(Path.of("elsewhere")).setTypeName("SomeOtherClass");

        StatisticsSummary<Perfume> summary = StatisticsSummary.from(registry);
        summary.addToStatistics(List.of(det1, det2, det3, det4));
        summary.addToStatistics(Path.of(".."));

        outputGenerator.complete(summary);

        Path summaryFile = OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR.resolve(Path.of("summary.json"));
        assertThat(Files.exists(summaryFile)).isTrue();

        StatisticsSummary<Perfume> deserializedSummary = readStatistics(new TypeReference<>() {
                                                                        }, summaryFile, registry,
                Perfume.class);

        assertThat(deserializedSummary).isNotNull();
        assertThat(deserializedSummary.getAnalyzedFiles()).hasSize(4);
        assertThat(deserializedSummary.getTotalAnalysedFiles()).isEqualTo(4);
        assertThat(deserializedSummary.getTotalDetections()).isEqualTo(4);

        StatisticsSummary.Statistics<Perfume> perf1Stats = deserializedSummary.getDetectableStatistics().get(perf1);
        StatisticsSummary.Statistics<Perfume> perf2Stats = deserializedSummary.getDetectableStatistics().get(perf2);

        assertThat(perf1Stats).isNotNull();
        assertThat(perf1Stats.getDetectable()).isEqualTo(perf1);
        assertThat(perf1Stats.getTotalDetections()).isEqualTo(2);
        assertThat(perf1Stats.getFilesWithDetection()).hasSize(1);
        assertThat(perf1Stats.getUniqueFilesWithDetection()).isEqualTo(1);

        assertThat(perf2Stats).isNotNull();
        assertThat(perf2Stats.getDetectable()).isEqualTo(perf2);
        assertThat(perf2Stats.getTotalDetections()).isEqualTo(2);
        assertThat(perf2Stats.getFilesWithDetection()).hasSize(2);
        assertThat(perf2Stats.getUniqueFilesWithDetection()).isEqualTo(2);
    }
}
