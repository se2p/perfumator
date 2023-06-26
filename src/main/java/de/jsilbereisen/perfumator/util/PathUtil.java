package de.jsilbereisen.perfumator.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtil {

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
    public static boolean isRelevantJavaFile(Path path) {
        boolean isJavaFile = isJavaSourceFile(path);
        if (!isJavaFile) {
            return false;
        }

        boolean isPackageInfo = path.getFileName().toString().equals("package-info.java");
        if (isPackageInfo) {
            return false;
        }

        boolean isResource = path.normalize().toString().contains(Path.of("src", "main",
                "resources").toString())
                || path.normalize().toString().contains(Path.of("src", "test",
                "resources").toString());
        return !isResource;
    }
}
