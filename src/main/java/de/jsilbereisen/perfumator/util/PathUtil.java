package de.jsilbereisen.perfumator.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;

public class PathUtil {

    public static final Path MAIN_RESOURCES = Path.of("src", "main", "resources");

    public static final Path TEST_RESOURCES = Path.of("src", "test", "resources");

    public static final Path MAIN_JAVA = Path.of("src", "main", "java");

    public static final Path TEST_JAVA = Path.of("src", "test", "java");

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
     * <i>package-info.java</i> file and if it's not in a <i>resources</i> directory.
     *
     * @param path The path to check.
     * @return {@code true} if the given {@link Path} represents an existing, relevant Java source file.
     */
    public static boolean isRelevantJavaFile(@NotNull Path path, @NotNull String analysisRootDirName) {
        boolean isJavaFile = isJavaSourceFile(path);
        if (!isJavaFile) {
            return false;
        }

        boolean isPackageInfo = path.getFileName().toString().equals("package-info.java");
        if (isPackageInfo) {
            return false;
        }

        return !isResourceInAnalysisRoot(path, analysisRootDirName);
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
}
