package test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine;
import de.jsilbereisen.perfumator.util.PathUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public abstract class AbstractDetectorTest {

    protected static final Path DEFAULT_DOT_DIR = Path.of("src", "test", "resources", "graphics", "dot");

    protected static final Path DEFAULT_DETECTOR_TEST_FILES_DIR = Path.of("src", "test", "resources", "detectors");

    protected JavaParser parser;

    @BeforeEach
    void initParser() {
        parser = PerfumeDetectionEngine.getConfiguredJavaParser();
    }

    protected static CompilationUnit parseAstForFile(@NotNull Path path) {
        final JavaParser parser = PerfumeDetectionEngine.getConfiguredJavaParser();

        return parseAstForFile(parser, path);
    }

    protected static CompilationUnit parseAstForFile(@NotNull JavaParser parser, @NotNull Path path) {
        assert PathUtil.isJavaSourceFile(path) : "Path does not point to an existing single Java Source file.";

        ParseResult<CompilationUnit> result;

        try {
            result = parser.parse(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (result == null || !result.isSuccessful() || result.getResult().isEmpty()) {
            if (result != null) {
                for (Problem p : result.getProblems()) {
                    log.error("Problem at \"" + p.getLocation().orElse(null) + "\" caused by \""
                            + p.getCause().orElse(null) + "\" with message:\n\"" + p.getVerboseMessage() + "\"");
                }

                throw new AssertionError("Problems encountered when parsing Java Source file at " + path);
            } else {
                throw new AssertionError("Parsing failed, result is \"null\" for file " + path);
            }
        }

        return result.getResult().get();
    }

    /**
     * Util method to quickly parse a Java Source file to an AST and save the AST as a ".dot" file.
     * Returns the parsed AST.
     */
    protected static CompilationUnit parseFileAndSaveDot(@NotNull Path javaSourceFile, @NotNull String fileName) {
        return parseFileAndSaveDot(javaSourceFile, DEFAULT_DOT_DIR, fileName);
    }

    /**
     * Util method to quickly parse a Java Source file to an AST and save the AST as a ".dot" file.
     * Returns the parsed AST.
     */
    protected static CompilationUnit parseFileAndSaveDot(@NotNull Path javaSourceFile, @NotNull Path saveDirectory,
                                                         @NotNull String fileName) {
        if (!PathUtil.isJavaSourceFile(javaSourceFile)) {
            throw new IllegalArgumentException(javaSourceFile + " is not a single Java Source file.");
        }

        CompilationUnit ast = parseAstForFile(javaSourceFile);
        saveAstAsDot(ast, saveDirectory, fileName);

        log.info("Saved DOT-file for parsed AST of " + javaSourceFile);

        return ast;
    }

    /**
     * Util method to quickly save an AST as a ".dot" file.
     */
    protected static void saveAstAsDot(@NotNull Node node, @NotNull String fileName) {
        saveAstAsDot(node, DEFAULT_DOT_DIR, fileName);
    }

    /**
     * Util method to quickly save an AST as a ".dot" file.
     */
    protected static void saveAstAsDot(@NotNull Node node, @NotNull Path saveDirectory, @NotNull String fileName) {
        DotPrinter printer = new DotPrinter(true);
        String dotString = printer.output(node);

        Path saveFilePath = saveDirectory.resolve(fileName + ".dot");

        try {
            Files.deleteIfExists(saveFilePath);
        } catch (IOException io) {
            io.printStackTrace();
        }

        try {
            Files.writeString(saveFilePath, dotString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Saved DOT-file");
    }

    /**
     * Sets up the given parser for symbol resolution with the given dependencies.<br/>
     * <b>CAUTION:</b> you need to create the analysis context <b>BEFORE</b> parsing the source file, in order for
     * the {@link CompilationUnit} being able to find the resolver. So, first call this method, and then call
     * {@link #parseAstForFile(JavaParser, Path)} with the <b>SAME</b> {@link JavaParser} instance.
     *
     * @param parser The parser.
     * @param dependencies A list of all dependencies that are relevant. A dependency can be a JAR or a root package of
     *                     Java source files. Otherwise, will probably throw an Exception or simply not work at runtime.
     * @return The {@link JavaParserFacade} context, with the configured {@link TypeSolver}.
     */
    @NotNull
    protected static JavaParserFacade getAnalysisContext(@NotNull JavaParser parser, @NotNull Path... dependencies) {
        // Very very ugly, but i don't see any other way to get the created TypeSolver at the moment
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        /*
        try {
            typeSolver = (CombinedTypeSolver) FieldUtils.readField(strategy, "typeSolver", true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
         */

        for (Path dependency : dependencies) {
            if (!Files.exists(dependency)) {
                throw new IllegalArgumentException("Dependency \"" + dependency + "\" does not exist.");
            }

            if (dependency.endsWith(".jar")) {
                JarTypeSolver jarSolver;
                try {
                    jarSolver = new JarTypeSolver(dependency);
                } catch (Exception e) {
                    throw new IllegalStateException("Dependency \"" + dependency + "\" failed to be loaded by the " +
                            "JarTypeSolver.", e);
                }
                typeSolver.add(jarSolver);

            } else {
                JavaParserTypeSolver jpTypeSolver = new JavaParserTypeSolver(dependency, parser.getParserConfiguration());
                typeSolver.add(jpTypeSolver);
            }
        }

        parser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
        return JavaParserFacade.get(typeSolver);
    }
}
