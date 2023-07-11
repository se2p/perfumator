package test;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Execution(ExecutionMode.SAME_THREAD)
public abstract class AbstractOutputTest {

    protected static final Path OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR = Path.of("src", "test", "resources",
            "io", "output", "test_results");

    protected static final Path OUTPUT_TEST_COMPARISON_RESOURCES_ROOT_DIR = Path.of("src", "test", "resources",
            "io", "output", "comparisons");

    protected static @NotNull List<Path> getDetectionsOutputPaths(@NotNull Pattern fileNamePattern) throws IOException {
        List<Path> outputFiles;
        try (Stream<Path> dirStream = Files.list(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR)) {
            outputFiles = dirStream
                    .filter(path -> Files.isRegularFile(path)
                            && fileNamePattern.matcher(path.getFileName().toString()).matches()
                            && !path.toString().endsWith(".gitkeep"))
                    .collect(Collectors.toList());
        }
        return outputFiles;
    }

    @BeforeEach
    @AfterEach
    void cleanTestDir() throws IOException {
        try (Stream<Path> fileWalk = Files.walk(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR)) {
            fileWalk.forEach(path -> {
                if (Files.isRegularFile(path) && !path.getFileName().toString().equals(".gitkeep")) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

}
