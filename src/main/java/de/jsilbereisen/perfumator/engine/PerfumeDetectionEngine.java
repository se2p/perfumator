package de.jsilbereisen.perfumator.engine;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import de.jsilbereisen.perfumator.model.AnalysisResult;
import de.jsilbereisen.perfumator.model.StatisticsSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.PathUtil;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static de.jsilbereisen.perfumator.util.PathUtil.toRealPath;

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

    /**
     * Limits the cache size of a {@link JavaParserTypeSolver}.
     * This value is more or less experimental - you might want to increase this for better performance.
     */
    public static final int JAVA_PARSER_CACHE_LIMIT = 100;

    @NotNull
    private final DetectableRegistry<Perfume> perfumeRegistry;

    @NotNull
    private final Bundles i18n;

    @NotNull
    @Unmodifiable
    private final List<Path> analysisDependencies;

    @Getter
    @Setter
    @Nullable
    private JavaParser astParser;

    @Getter
    @Nullable
    private JavaParserFacade analysisContext;

    private PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @NotNull Bundles bundles,
                                   @NotNull JavaParser astParser, @NotNull List<Path> dependencies) {
        this.perfumeRegistry = perfumeRegistry;
        this.astParser = astParser;
        this.i18n = bundles;
        this.analysisDependencies = Collections.unmodifiableList(dependencies);
    }

    /**
     * Returns a builder with default settings and the default language {@link LanguageTag#getDefault()}.
     *
     * @return the builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(@NotNull Locale locale) {
        return new Builder(locale);
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
        config.setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver(false)));
        config.setDoNotAssignCommentsPrecedingEmptyLines(false);

        return new JavaParser(config);
    }

    @Override
    @NotNull
    public AnalysisResult<Perfume> detect(@NotNull Path sources) {
        if (!(Files.isDirectory(sources) || PathUtil.isJavaSourceFile(sources))) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.invalidSourcesPath"));
        }

        StatisticsSummary<Perfume> summary = StatisticsSummary.from(perfumeRegistry);
        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        // Has to be recreated to include the given sources
        analysisContext = createAnalysisContext(sources, analysisDependencies);

        StopWatch timer = StopWatch.create();
        timer.start();

        if (Files.isDirectory(sources)) {
            try (Stream<Path> dirWalk = Files.walk(sources)) {
                dirWalk.filter(path -> PathUtil.isRelevantJavaFile(path, sources.getFileName().toString()))
                        .forEach(sourceFile -> {
                            List<DetectedInstance<Perfume>> detections = detectInSingleSourceFile(sourceFile);

                            // Keep statistics
                            toRealPath(sourceFile).ifPresentOrElse(
                                    summary::addToStatistics,
                                    () -> summary.addToStatistics(sourceFile)
                            );
                            summary.addToStatistics(detections);

                            detectedPerfumes.addAll(detections);
                        });

            } catch (Exception e) {
                log.error(i18n.getApplicationResource("log.error.analysis.unknown"));
                throw new AnalysisException(e.getMessage(), e);
            }

        } else {
            List<DetectedInstance<Perfume>> detections = detectInSingleSourceFile(sources);

            summary.addToStatistics(sources);
            summary.addToStatistics(detections);

            detectedPerfumes.addAll(detections);
        }

        timer.stop();
        Path analysisPath = toRealPath(sources).orElse(sources);
        log.info(i18n.getApplicationResource("log.info.analysis.done"), analysisPath, timer.getTime(TimeUnit.SECONDS));

        return new AnalysisResult<>(detectedPerfumes, summary);
    }

    @Override
    public void detectAndSerialize(@NotNull Path sources, @NotNull OutputConfiguration config,
                                   @NotNull OutputFormat format) throws SerializationException {
        if (!(Files.isDirectory(sources) || PathUtil.isJavaSourceFile(sources))) {
            throw new IllegalArgumentException(i18n.getApplicationResource("exception.invalidSourcesPath"));
        }

        // Check whether the config is valid and create an Output Generator for the desired format, if supported
        checkOutputConfig(config);
        OutputGenerator<Perfume> outputGenerator = getOutputGenerator(config, format);

        StatisticsSummary<Perfume> summary = StatisticsSummary.from(perfumeRegistry);

        analysisContext = createAnalysisContext(sources, analysisDependencies);

        StopWatch timer = StopWatch.create();
        timer.start();

        if (Files.isDirectory(sources)) {
            List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

            try (Stream<Path> dirWalk = Files.walk(sources)) {
                dirWalk.filter(path -> PathUtil.isRelevantJavaFile(path, sources.getFileName().toString())).forEach(sourceFile -> {
                    List<DetectedInstance<Perfume>> detections = detectInSingleSourceFile(sourceFile);

                    // Keep statistics
                    toRealPath(sourceFile).ifPresentOrElse(
                            summary::addToStatistics,
                            () -> summary.addToStatistics(sourceFile)
                    );
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

        timer.stop();
        Path analysisPath = toRealPath(sources).orElse(sources);
        log.info(i18n.getApplicationResource("log.info.analysis.done"), analysisPath, timer.getTime(TimeUnit.SECONDS));

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

            List<DetectedInstance<Perfume>> detections;
            try {
                detections = detector.detect(ast);
            } catch (Throwable t) {
                // We want to catch EVERYTHING here, e.g. also StackOverflowError, just to be able to
                // give it additional context by giving the file name that was being analysed + the detector.
                throw new AnalysisException("Exception when analysing source file " + javaSourceFilePath
                        + " with detector " + detector.getClass().getSimpleName(), t);
            }

            detections.forEach(det -> toRealPath(javaSourceFilePath).ifPresentOrElse(
                    det::setSourceFile,
                    () -> det.setSourceFile(javaSourceFilePath)
            ));

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
    private JavaParserFacade createAnalysisContext(@NotNull Path sources, @NotNull List<Path> dependencies) {
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

            if (dependency.toString().endsWith(".jar")) {
                JarTypeSolver jarSolver;
                try {
                    jarSolver = new JarTypeSolver(dependency);
                } catch (Exception e) {
                    log.error(i18n.getApplicationResource("log.error.analysis.dependencyUnresolvable"), dependency);
                    continue;
                }
                typeSolver.add(jarSolver);

            } else {
                JavaParserTypeSolver jpTypeSolver = new JavaParserTypeSolver(dependency, astParser.getParserConfiguration(),
                        JAVA_PARSER_CACHE_LIMIT);
                typeSolver.add(jpTypeSolver);
            }
        }

        return JavaParserFacade.get(typeSolver);
    }

    public static class Builder {

        private DetectableRegistry<Perfume> perfumeRegistry;

        private Bundles i18n;

        private JavaParser astParser;

        private List<Path> dependencies;

        /**
         * Constructor, sets the default engine state (loads the default {@link Perfume}s and resources with the
         * default locale, specified by {@link LanguageTag#getDefault()}).
         */
        public Builder() {
            this(LanguageTag.getDefault().getRelatedLocale());
        }

        /**
         * Constructor, sets the default engine state (loads the default {@link Perfume}s and resources with the
         * given {@link Locale}).
         */
        public Builder(@NotNull Locale locale) {
            perfumeRegistry = new PerfumeRegistry();
            perfumeRegistry.loadRegistry(locale);

            i18n = new Bundles();
            BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE, BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
            bundlesLoader.loadApplicationBundle(i18n, locale);

            astParser = getConfiguredJavaParser();

            dependencies = new ArrayList<>();
        }

        /**
         * Sets the registry. Does <b>not</b> call {@link DetectableRegistry#loadRegistry}.
         *
         * @param registry The registry to use.
         * @return {@code this}.
         */
        @NotNull
        public Builder registry(@NotNull DetectableRegistry<Perfume> registry) {
            this.perfumeRegistry = registry;
            return this;
        }

        @NotNull
        public Builder i18nResources(@NotNull Bundles i18nResources) {
            this.i18n = i18nResources;
            return this;
        }

        @NotNull
        public Builder javaParser(@NotNull JavaParser parser) {
            this.astParser = parser;
            return this;
        }

        @NotNull
        public Builder setDependencies(@NotNull Collection<Path> dependencies) {
            this.dependencies = new ArrayList<>(dependencies);
            return this;
        }

        @NotNull
        public Builder setDependencies(@NotNull Path... dependencies) {
            this.dependencies = new ArrayList<>(List.of(dependencies));
            return this;
        }

        @NotNull
        public Builder addDependency(@NotNull Path dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        @NotNull
        public Builder clearDependencies() {
            this.dependencies.clear();
            return this;
        }

        @NotNull
        public PerfumeDetectionEngine build() {
            return new PerfumeDetectionEngine(perfumeRegistry, i18n, astParser, dependencies);
        }
    }
}
