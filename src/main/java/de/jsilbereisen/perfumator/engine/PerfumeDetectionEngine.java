package de.jsilbereisen.perfumator.engine;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import de.jsilbereisen.perfumator.io.LanguageTag;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.PathUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Engine for detecting {@link Perfume}s. Uses the <i>JavaParser</i> library for parsing
 * source files into an AST.<br/>
 * Default language level for AST parsing is Java 17 (maximum possible for {@link JavaParser}).
 * If you want to lower the language level/use another {@link ParserConfiguration},
 * configure a {@link JavaParser} instance at you will and call the engine's setter <i>before</i>
 * calling the {@link #detect} or the {@link #detectInSingleSourceFile} method.
 */
@Slf4j
public class PerfumeDetectionEngine implements DetectionEngine<Perfume> {

    private final DetectableRegistry<Perfume> perfumeRegistry;

    private final Locale locale;

    private final Bundles i18n;

    @Getter
    @Setter
    private JavaParser astParser;

    public PerfumeDetectionEngine() {
        this.perfumeRegistry = new PerfumeRegistry();
        this.locale = LanguageTag.getDefault().getRelatedLocale();
        this.astParser = getConfiguredJavaParser();
        this.i18n = new Bundles();

        perfumeRegistry.loadRegistry(locale);

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, locale);
    }

    public PerfumeDetectionEngine(@Nullable Locale locale) {
        this.perfumeRegistry = new PerfumeRegistry();
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = getConfiguredJavaParser();
        this.i18n = new Bundles();

        perfumeRegistry.loadRegistry(this.locale);

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, this.locale);
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale) {
        this.perfumeRegistry = perfumeRegistry;
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = getConfiguredJavaParser();
        this.i18n = new Bundles();

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, this.locale);
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale,
                                  @NotNull Bundles bundles) {
        this.perfumeRegistry = perfumeRegistry;
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = getConfiguredJavaParser();
        this.i18n = bundles;
    }

    @Override
    public List<DetectedInstance<Perfume>> detect(@NotNull Path sources) {
        if (!(Files.isDirectory(sources) || PathUtil.isJavaSourceFile(sources))) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.invalidSourcesPath"));
        }

        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        if (Files.isDirectory(sources)) {
            try (Stream<Path> dirWalk = Files.walk(sources)) {
                dirWalk.filter(PathUtil::isJavaSourceFile)
                        .forEach(sourceFile -> detectedPerfumes.addAll(detectInSingleSourceFile(sourceFile)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            detectedPerfumes.addAll(detectInSingleSourceFile(sources));
        }

        return detectedPerfumes;
    }

    public List<DetectedInstance<Perfume>> detectInSingleSourceFile(@NotNull Path javaSourceFilePath) {
        if (!PathUtil.isJavaSourceFile(javaSourceFilePath)) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.notJavaSourceFile"));
        }

        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        // Parse source file to AST
        ParseResult<CompilationUnit> parseResult;
        try {
            parseResult = astParser.parse(javaSourceFilePath);
        } catch (ParseProblemException e) {
            log.error(i18n.getApplicationResource("log.parseFailed.prePath") + javaSourceFilePath
                    + i18n.getApplicationResource("log.parseFailed.postPath"));
            return Collections.emptyList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assert parseResult != null;

        CompilationUnit ast;
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            ast = parseResult.getResult().get();
        } else {
            log.error(i18n.getApplicationResource("log.parseFailed.prePath") + javaSourceFilePath
                    + i18n.getApplicationResource("log.parseFailed.postPath"));

            for (Problem problem : parseResult.getProblems()) {
                log.error(i18n.getApplicationResource("log.parseProblem") + " " + problem.getVerboseMessage());
            }

            return Collections.emptyList();
        }

        // Apply all Detectors on the AST
        for (Detector<Perfume> detector : perfumeRegistry.getRegisteredDetectors()) {
            detectedPerfumes.addAll(detector.detect(ast));
        }

        return detectedPerfumes;
    }

    /**
     * Returns a new {@link JavaParser} instance with the {@link ParserConfiguration} configured
     * as it is used by the <i><b>Perfumator</b></i> by default.<br/>
     * List of settings that are changed, compared to the default {@link ParserConfiguration}:
     * <ul>
     *     <li>Language level: 11 (default) -&gt; 17</li>
     *     <li>Do NOT Capture empty line comments: {@code true} -&gt; {@code false}</li>
     * </ul>
     *
     * @return The new, configured {@link JavaParser} instance.
     */
    @NotNull
    public static JavaParser getConfiguredJavaParser() {
        ParserConfiguration config = new ParserConfiguration();

        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        config.setDoNotAssignCommentsPrecedingEmptyLines(false);

        return new JavaParser(config);
    }
}
