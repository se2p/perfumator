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
}
