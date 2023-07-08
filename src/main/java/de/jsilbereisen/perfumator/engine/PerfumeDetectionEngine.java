package de.jsilbereisen.perfumator.engine;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.registry.DetectableRegistry;
import de.jsilbereisen.perfumator.engine.registry.PerfumeRegistry;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import de.jsilbereisen.perfumator.io.LanguageTag;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.io.output.OutputGenerator;
import de.jsilbereisen.perfumator.io.output.json.PerfumeJsonOutputGenerator;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.PathUtil;

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
 * configure a {@link JavaParser} instance at your will and call the engine's setter <i>before</i>
 * calling the {@link #detect}, {@link #detectAndSerialize} or the {@link #detectInSingleSourceFile} method.
 */
@Slf4j
public class PerfumeDetectionEngine implements DetectionEngine<Perfume> {

    @NotNull
    private final DetectableRegistry<Perfume> perfumeRegistry;

    @NotNull
    private final Locale locale;

    @NotNull
    private final Bundles i18n;

    @Getter
    @Setter
    @Nullable
    private JavaParser astParser;

    @Getter
    @Setter
    @Nullable
    private JavaParserFacade analysisContext;

    public PerfumeDetectionEngine() {
        this(LanguageTag.getDefault().getRelatedLocale());
    }

    public PerfumeDetectionEngine(@Nullable Locale locale) {
        this(new PerfumeRegistry(), locale);

        perfumeRegistry.loadRegistry(this.locale);
    }

    public PerfumeDetectionEngine(@Nullable Locale locale, @NotNull Bundles bundles) {
        this(new PerfumeRegistry(), locale, bundles);

        perfumeRegistry.loadRegistry(this.locale);
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale) {
        this(perfumeRegistry, locale, new Bundles());

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE, BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, this.locale);
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale, @NotNull Bundles bundles) {
        this(perfumeRegistry, locale, bundles, getConfiguredJavaParser());
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale, @NotNull Bundles bundles, @NotNull JavaParser astParser) {
        this.perfumeRegistry = perfumeRegistry;
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = astParser;
        this.i18n = bundles;
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

    @Override
    public List<DetectedInstance<Perfume>> detect(@NotNull Path sources, @NotNull Path... dependencies) {
        if (!(Files.isDirectory(sources) || PathUtil.isJavaSourceFile(sources))) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.invalidSourcesPath"));
        }

        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        analysisContext = createAnalysisContext(sources, dependencies);

        if (Files.isDirectory(sources)) {
            try (Stream<Path> dirWalk = Files.walk(sources)) {
                dirWalk.filter(path -> PathUtil.isRelevantJavaFile(path, sources.getFileName().toString()))
                        .forEach(sourceFile -> detectedPerfumes.addAll(detectInSingleSourceFile(sourceFile)));

            } catch (Exception e) {
                log.error(i18n.getApplicationResource("log.error.analysis.unknown"));
                throw new AnalysisException(e.getMessage(), e);
            }

        } else {
            detectedPerfumes.addAll(detectInSingleSourceFile(sources));
        }

        return detectedPerfumes;
    }

    @Override
    public void detectAndSerialize(@NotNull Path sources, @NotNull OutputConfiguration config,
                                   @NotNull OutputFormat format, @NotNull Path... dependencies)
            throws SerializationException {
        if (!(Files.isDirectory(sources) || PathUtil.isJavaSourceFile(sources))) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.invalidSourcesPath"));
        }

        // Check whether the config is valid and create an Output Generator for the desired format, if supported
        checkOutputConfig(config);
        OutputGenerator<Perfume> outputGenerator = getOutputGenerator(config, format);

        StatisticsSummary<Perfume> summary = StatisticsSummary.from(perfumeRegistry);

        analysisContext = createAnalysisContext(sources, dependencies);

        if (Files.isDirectory(sources)) {
            List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

            try (Stream<Path> dirWalk = Files.walk(sources)) {
                dirWalk.filter(path -> PathUtil.isRelevantJavaFile(path, sources.getFileName().toString())).forEach(sourceFile -> {
                    List<DetectedInstance<Perfume>> detections = detectInSingleSourceFile(sourceFile);

                    // Keep statistics
                    summary.addToStatistics(sourceFile);
                    summary.addToStatistics(detections);

                    detectedPerfumes.addAll(detections);

                    // Generate Listing if batch size has already been reached, clear list
                    if (detectedPerfumes.size() >= outputGenerator.getConfig().getBatchSize()) {
                        generateListing(detectedPerfumes, outputGenerator);
                        detectedPerfumes.clear();
                    }
                });

            } catch (Exception e) {
                log.error(i18n.getApplicationResource("log.error.analysis.unknown"));
                throw new AnalysisException(e.getMessage(), e);
            }

            // Generate final listing when analysis is complete
            generateListing(detectedPerfumes, outputGenerator);

        } else {
            List<DetectedInstance<Perfume>> detections = detectInSingleSourceFile(sources);

            summary.addToStatistics(sources);
            summary.addToStatistics(detections);

            generateListing(detections, outputGenerator);
        }

        // Generate Summary
        try {
            outputGenerator.complete(summary);

        } catch (IOException e) {
            log.error(i18n.getApplicationResource("log.error.serialization.complete"));
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @NotNull
    public List<DetectedInstance<Perfume>> detectInSingleSourceFile(@NotNull Path javaSourceFilePath) {
        if (!PathUtil.isJavaSourceFile(javaSourceFilePath)) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.notJavaSourceFile"));
        }

        if (astParser == null) {
            astParser = getConfiguredJavaParser();
        }

        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        // Parse source file to AST
        ParseResult<CompilationUnit> parseResult;
        try {
            parseResult = astParser.parse(javaSourceFilePath);
        } catch (ParseProblemException e) {
            log.error(i18n.getApplicationResource("log.error.parse.prePath") + javaSourceFilePath + i18n.getApplicationResource("log.error.parse.postPath"));
            return Collections.emptyList();
        } catch (IOException e) {
            log.error(i18n.getApplicationResource("log.error.analysis.unknown"));
            throw new AnalysisException(e.getMessage(), e);
        }

        assert parseResult != null;

        CompilationUnit ast;
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            ast = parseResult.getResult().get();
        } else {
            log.error(i18n.getApplicationResource("log.error.parse.prePath") + javaSourceFilePath + i18n.getApplicationResource("log.error.parse.postPath"));

            for (Problem problem : parseResult.getProblems()) {
                log.error(i18n.getApplicationResource("log.error.parse.problem") + " " + problem.getVerboseMessage());
            }

            return Collections.emptyList();
        }

        // Apply all Detectors on the AST
        for (Detector<Perfume> detector : perfumeRegistry.getRegisteredDetectors()) {
            detector.setAnalysisContext(analysisContext);
            List<DetectedInstance<Perfume>> detections = detector.detect(ast);
            detections.forEach(det -> det.setSourceFile(javaSourceFilePath));

            detectedPerfumes.addAll(detections);
        }

        return detectedPerfumes;
    }

    @Override
    @NotNull
    public DetectableRegistry<Perfume> getRegistry() {
        return perfumeRegistry;
    }

    /**
     * Checks the output path of the given {@link OutputConfiguration}.
     * Path is only valid if it points to an empty (except ".gitkeep" files) directory.
     *
     * @param config The config with the output path.
     */
    private void checkOutputConfig(@NotNull OutputConfiguration config) {
        if (!Files.isDirectory(config.getOutputDirectory())) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.output.dirNotExists"));
        } else {
            boolean isNotEmpty = false;

            try (Stream<Path> paths = Files.list(config.getOutputDirectory())) {
                isNotEmpty = paths.anyMatch(path -> !path.getFileName().toString().endsWith(".gitkeep"));

            } catch (IOException e) {
                log.error(i18n.getApplicationResource("log.error.analysis.unknown"));
            }

            if (isNotEmpty) {
                throw new IllegalArgumentException(i18n.getApplicationResource("exception.output.dirNotEmpty"));
            }
        }
    }

    private OutputGenerator<Perfume> getOutputGenerator(@NotNull OutputConfiguration config,
                                                        @NotNull OutputFormat format) {
        if (format == OutputFormat.CSV) {
            throw new UnsupportedOperationException(i18n.getApplicationResource("exception.output.unsupportedFormat")
                    + " " + format.getAbbreviation());
        } else {
            return new PerfumeJsonOutputGenerator(config, i18n);
        }
    }

    private void generateListing(List<DetectedInstance<Perfume>> detections, OutputGenerator<Perfume> generator) throws SerializationException {
        try {
            generator.handle(detections);
        } catch (IOException e) {
            log.error(i18n.getApplicationResource("log.error.serialization.handle"));
            throw new SerializationException(e.getMessage(), e);
        }
    }

    /**
     * Creates a context for resolving symbols from the provided source file/directory and the provided dependencies.
     * A dependency must either be a JAR Archive or the root package of Java Source files - but be careful, the
     * latter is not validated!
     */
    @NotNull
    private JavaParserFacade createAnalysisContext(@NotNull Path sources, @NotNull Path... dependencies) {
        if (astParser == null) {
            astParser = getConfiguredJavaParser();
        }

        SymbolSolverCollectionStrategy strategy = new SymbolSolverCollectionStrategy(astParser.getParserConfiguration());
        strategy.collect(sources);

        // Very very ugly, but i don't see any other way to get the created TypeSolver at the moment
        CombinedTypeSolver typeSolver;
        try {
            typeSolver = (CombinedTypeSolver) FieldUtils.readField(strategy, "typeSolver", true);
        } catch (IllegalAccessException e) {
            log.error(i18n.getApplicationResource("log.error.analysis.unknown"));
            throw new AnalysisException(e.getMessage(), e);
        }

        for (Path dependency : dependencies) {
            if (!Files.exists(dependency)) {
                log.error(i18n.getApplicationResource("log.error.analysis.nonExistentDependency"));
            }

            if (dependency.endsWith(".jar")) {
                JarTypeSolver jarSolver;
                try {
                    jarSolver = new JarTypeSolver(dependency);
                } catch (Exception e) {
                    log.error(i18n.getApplicationResource("log.error.analysis.dependencyUnresolvable"), dependency);
                    continue;
                }
                typeSolver.add(jarSolver);

            } else {
                // TODO Cache limit ?
                JavaParserTypeSolver jpTypeSolver = new JavaParserTypeSolver(dependency, astParser.getParserConfiguration());
                typeSolver.add(jpTypeSolver);
            }
        }

        return JavaParserFacade.get(typeSolver);
    }
}
