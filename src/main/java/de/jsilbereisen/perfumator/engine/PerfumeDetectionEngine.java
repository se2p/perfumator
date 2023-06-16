package de.jsilbereisen.perfumator.engine;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
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

@Slf4j
public class PerfumeDetectionEngine implements DetectionEngine<Perfume> {

    private final DetectableRegistry<Perfume> perfumeRegistry;

    private final Locale locale;

    private final JavaParser astParser;

    private final Bundles i18n;

    public PerfumeDetectionEngine() {
        this.perfumeRegistry = new PerfumeRegistry();
        this.locale = LanguageTag.getDefault().getRelatedLocale();
        this.astParser = new JavaParser();
        this.i18n = new Bundles();

        perfumeRegistry.loadRegistry(locale);

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, locale);
    }

    public PerfumeDetectionEngine(@Nullable Locale locale) {
        this.perfumeRegistry = new PerfumeRegistry();
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = new JavaParser();
        this.i18n = new Bundles();

        perfumeRegistry.loadRegistry(this.locale);

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, this.locale);
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale) {
        this.perfumeRegistry = perfumeRegistry;
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = new JavaParser();
        this.i18n = new Bundles();

        BundlesLoader bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE, BundlesLoader.STANDARD_APPLICATION_PACKAGE);
        bundlesLoader.loadApplicationBundle(i18n, this.locale);
    }

    public PerfumeDetectionEngine(@NotNull DetectableRegistry<Perfume> perfumeRegistry, @Nullable Locale locale,
                                  @NotNull Bundles bundles) {
        this.perfumeRegistry = perfumeRegistry;
        this.locale = locale != null ? locale : LanguageTag.getDefault().getRelatedLocale();
        this.astParser = new JavaParser();
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
}
