package de.jsilbereisen.perfumator.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class PathUtil {

    public static final Path MAIN_RESOURCES = Path.of("src", "main", "resources");

    public static final Path TEST_RESOURCES = Path.of("src", "test", "resources");

    public static final Path MAIN_JAVA = Path.of("src", "main", "java");

    public static final Path TEST_JAVA = Path.of("src", "test", "java");

    public static final String MAVEN_TARGET_DIR_NAME = "target";

    public static final String GRADLE_BUILD_DIR_NAME = "build";

    public static final Set<String> MAVEN_TARGET_SUBDIRS = Set.of("archive-tmp", "classes", "generated-sources",
            "generated-test-sources", "maven-archiver", "maven-status", "test-classes");

    public static final Set<String> GRADLE_TARGET_SUBDIRS = Set.of("classes", "generated", "jacoco",
            "javadoc", "libs", "reports", "test-results");

    public static final String PACKAGE_INFO = "package-info.java";

    public static final String MODULE_INFO = "module-info.java";

    private PathUtil() {

    }

    /**
     * Checks whether the given {@link Path} links to a Java source code file.
     *
     * @param path The {@link Path} to check.
     * @return {@code true} if the {@link Path} points to a Java source code file.
     */
    public static boolean isJavaSourceFile(@NotNull Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java");
    }

    /**
     * Checks whether the given {@link Path} represents a relevant Java source file, in the
     * context of static analysis. A Java source file is seen as relevant if it is not a
     * <i>package-info.java</i> file and if it's not in a <i>resources</i> or <i>target</i> directory.
     *
     * @param path The path to check.
     * @return {@code true} if the given {@link Path} represents an existing, relevant Java source file.
     */
    public static boolean isRelevantJavaFile(@NotNull Path path, @NotNull String analysisRootDirName) {
        boolean isJavaFile = isJavaSourceFile(path);
        if (!isJavaFile) {
            return false;
        }

        String fileNameOfPath = path.getFileName().toString();
        if (fileNameOfPath.equals(PACKAGE_INFO) || fileNameOfPath.equals(MODULE_INFO)) {
            return false;
        }

        if (isInBuildOutputDir(path, analysisRootDirName)) {
            return false;
        }

        return !isResourceInAnalysisRoot(path, analysisRootDirName);
    }

    private static boolean isInBuildOutputDir(@NotNull Path path, @NotNull String analysisRootDirName) {
        Path normalizedPath = path.normalize();

        boolean passedAnalysisRootDir = false;

        // Iterate over all name elements of the path. Looks for target-directory-structure
        // AFTER the analysis root dir
        for (int i = 0; i < normalizedPath.getNameCount(); i++) {
            Path current = normalizedPath.getName(i);

            if (current.toString().equals(analysisRootDirName)) {
                passedAnalysisRootDir = true;
            }

            // Look for build output dir
            if (passedAnalysisRootDir) {
                if ((current.toString().equals(MAVEN_TARGET_DIR_NAME) || current.toString().equals(GRADLE_BUILD_DIR_NAME))
                        && i < normalizedPath.getNameCount() - 1) {
                    Path targetDirSubtpath = normalizedPath.subpath(0, i + 1);

                    if (!Files.isDirectory(targetDirSubtpath)) {
                        continue;
                    }

                    // Validate that the detected "target" directory has some typical maven build output directory.
                    // We dont want to ignore a source-code package just because it is named "target"
                    boolean hasTypicalTargetSubdir;
                    Set<String> typicalSubdirs = switch (current.toString()) {
                        case MAVEN_TARGET_DIR_NAME -> MAVEN_TARGET_SUBDIRS;
                        case GRADLE_BUILD_DIR_NAME -> GRADLE_TARGET_SUBDIRS;
                        default -> Collections.emptySet();
                    };

                    try (Stream<Path> subdirs = Files.list(targetDirSubtpath)) {
                        hasTypicalTargetSubdir = subdirs.anyMatch(pathInTargetDir -> Files.isDirectory(pathInTargetDir)
                                && typicalSubdirs.contains(pathInTargetDir.getFileName().toString()));
                    } catch (IOException e) {
                        // ignored
                        continue;
                    }

                    if (hasTypicalTargetSubdir) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks whether the file (represented by the given {@link Path}) lies in a resource-directory
     * (src/main/resources or src/test/resources) of the project/directory that is being analysed.
     */
    private static boolean isResourceInAnalysisRoot(@NotNull Path path, @NotNull String analysisRootDirName) {
        Path normalizedPath = path.normalize();

        Deque<String> previousTwo = new LinkedList<>();
        boolean passedAnalysisRootDir = false;

        // Iterate over all name elements of the path. Looks for resource-directory-structure
        // AFTER the analysis root dir
        for (Path current : normalizedPath) {
            if (current.toString().equals(analysisRootDirName)) {
                passedAnalysisRootDir = true;
            }

            // After passing the root dir, look if any 3 consecutive dirs together match a Resource path
            if (passedAnalysisRootDir) {
                if (previousTwo.size() == 2) {
                    Path normalizedSubpath = Path.of(previousTwo.getLast(), previousTwo.getFirst(),
                            current.toString()).normalize();

                    if (normalizedSubpath.equals(MAIN_RESOURCES) || normalizedSubpath.equals(TEST_RESOURCES)) {
                        return true;
                    }
                }

                previousTwo.addFirst(current.toString());

                if (previousTwo.size() > 2) {
                    previousTwo.removeLast();
                }
            }
        }

        return false;
    }

    /**
     * Extracts the sub-path from the given {@link Path} which represents the Path that starts with the first package.
     * In other words, removes everything before and including "src/main/java" or "src/test/java" and returns the subpath.
     * If none of those Strings is found, just returns the given path.
     * <br/>
     * Example: From "some_dir/some_project/src/main/java/de/example/SomeClass.java" extracts "de/example/SomeClass.java".
     */
    public static Path toPackagePath(@NotNull Path path) {
        String pathAsString = path.toString();
        if (!pathAsString.contains(MAIN_JAVA.toString())
                && !pathAsString.contains(TEST_JAVA.toString())) {
            return path;
        }

        String separator = FileSystems.getDefault().getSeparator();

        int indexOf = pathAsString.lastIndexOf(MAIN_JAVA.toString());
        Path emptyPath = Path.of("");

        if (indexOf != -1) {
            int beginIndex = indexOf + MAIN_JAVA.toString().length() + separator.length();

            if (beginIndex < pathAsString.length()) {
                return Path.of(pathAsString.substring(beginIndex));
            } else {
                // If the given path has no package after the "src/.../java", return an empty path
                return emptyPath;
            }
        }

        indexOf = pathAsString.lastIndexOf(TEST_JAVA.toString());
        if (indexOf != -1) {
            int beginIndex = indexOf + TEST_JAVA.toString().length() + separator.length();

            if (beginIndex < pathAsString.length()) {
                return Path.of(pathAsString.substring(beginIndex));
            } else {
                // If the given path has no package after the "src/.../java", return an empty path
                return emptyPath;
            }
        }

        return path;
    }

    /**
     * Converts the given {@link Path} to a "real", unique {@link Path} by calling {@link Path#toRealPath}
     * without any {@link LinkOption}s.
     *
     * @param path The {@link Path} to convert.
     * @return An {@link Optional} with the converted, real {@link Path} if successful and {@link Optional#empty()}
     *         otherwise.
     */
    @NotNull
    public static Optional<Path> toRealPath(@NotNull Path path) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try {
            return Optional.of(path.toRealPath());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
