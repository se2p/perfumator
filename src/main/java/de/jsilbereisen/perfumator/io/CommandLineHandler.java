package de.jsilbereisen.perfumator.io;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import de.jsilbereisen.perfumator.model.EngineConfiguration;
import de.jsilbereisen.perfumator.util.PathUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.jsilbereisen.perfumator.util.PathUtil.toRealPath;


/**
 * Handles the command line on application start and invokes the appropriate actions, depending on the arguments and
 * options.
 */
@Slf4j
public class CommandLineHandler {

    private final CmdLineParser parser;

    private final Bundles cliResourceHolder;

    private final BundlesLoader bundlesLoader;

    public CommandLineHandler(@NotNull CmdLineParser parser, @NotNull Bundles cliResourceHolder) {
        this.parser = parser;
        this.cliResourceHolder = cliResourceHolder;
        this.bundlesLoader = new BundlesLoader(BundlesLoader.STANDARD_INTERNATIONALIZATION_PACKAGE,
                BundlesLoader.STANDARD_PERFUMES_PACKAGE);
    }

    // TODO: refactor method

    /**
     * Handles the given application input according to the set options.
     */
    public @Nullable EngineConfiguration handleArguments(@NotNull CommandLineInput cliInput) {
        Locale applicationLocale = cliInput.getLocale();
        String applicationLanguageName = LanguageTag.of(applicationLocale).getFullLanguageName();

        bundlesLoader.loadCliBundle(cliResourceHolder, applicationLocale);
        ResourceBundle cliBundle = cliResourceHolder.getCliBundle();
        assert cliBundle != null;

        log.info(cliBundle.getString("log.generic.locale"), applicationLanguageName);

        if (cliInput.isPrintHelp()) {
            printHelp();
            log.info(cliBundle.getString("log.generic.terminate"));
            return null;
        }

        Path inputPath = cliInput.getPathToSourceDir();
        Path outputPath = cliInput.getPathToOutputDir();

        boolean isInputPathValid = checkInputPath(inputPath);
        boolean isOutputPathValid = checkOutputPath(outputPath);
        if (!isInputPathValid || !isOutputPathValid) {
            printHelp();
            log.error("\n" + cliBundle.getString("log.generic.terminate"));
            return null;
        }

        toRealPath(inputPath).ifPresentOrElse(path -> log.info(cliBundle.getString("log.generic.inputPath"), path),
                () -> log.info(cliBundle.getString("log.generic.inputPath"), inputPath.toAbsolutePath()));
        toRealPath(outputPath).ifPresentOrElse(path -> log.info(cliBundle.getString("log.generic.outputPath"), path),
                () -> log.info(cliBundle.getString("log.generic.outputPath"), outputPath.toAbsolutePath()));
        log.info(cliBundle.getString("log.generic.outputFormat"), cliInput.getOutputFormat().getAbbreviation());

        return new EngineConfiguration(inputPath, outputPath, applicationLocale, cliInput.getOutputFormat());
    }

    /**
     * Gets called when a {@link CmdLineException} is thrown when parsing the command line on application startup.
     * Tries to extract a language option from the command line arguments, to set the {@link Locale} for loading the
     * command line resources accordingly. If no valid language option is given, the resources are loaded with the default
     * application language (extracted from {@link LanguageTag#getDefault()}).<br/>
     * Prints out the un-internationalized error message,
     * then prints out the usage text from the loaded resources.
     */
    public void handleError(@NotNull String[] args, @NotNull CmdLineException cliException) {
        Locale locale = LanguageTag.getDefault().getRelatedLocale();

        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-l") || args[i].equals("--language")) && i + 1 < args.length) {
                locale = LanguageTag.of(args[i + 1]).getRelatedLocale();
                break;
            }
        }

        bundlesLoader.loadCliBundle(cliResourceHolder, locale);
        ResourceBundle cliBundle = cliResourceHolder.getCliBundle();
        assert cliBundle != null;

        log.error(cliBundle.getString("log.error.unableToHandleInput"));
        log.error(cliException.getMessage() + "\n");
        printHelp();

        log.error(cliBundle.getString("log.generic.terminate"));
    }

    private void printHelp() {
        ResourceBundle cliBundle = cliResourceHolder.getCliBundle();
        assert cliBundle != null;

        log.info(cliBundle.getString("log.generic.preHelp"));
        parser.printUsage(new OutputStreamWriter(System.out), cliBundle);
    }

    private boolean checkInputPath(@Nullable Path path) {
        ResourceBundle cliBundle = cliResourceHolder.getCliBundle();
        assert cliBundle != null;

        if (!checkForNullAndExists(path, cliBundle.getString("log.error.inputPathMissing"),
                cliBundle.getString("log.error.invalidInputPath"))) {
            return false;
        }
        assert path != null;

        if (!Files.isDirectory(path) && !PathUtil.isJavaSourceFile(path)) {
            log.error(cliBundle.getString("log.error.invalidInputPath"));
            return false;
        }

        return true;
    }

    private boolean checkOutputPath(@Nullable Path path) {
        ResourceBundle cliBundle = cliResourceHolder.getCliBundle();
        assert cliBundle != null;

        if (!checkForNullAndExists(path, cliBundle.getString("log.error.outputPathMissing"),
                cliBundle.getString("log.error.invalidOutputPath"))) {
            return false;
        }

        assert path != null;

        if (!Files.isDirectory(path)) {
            log.error(cliBundle.getString("log.error.invalidOutputPath"));
            return false;
        } else {
            boolean isNotEmpty = true;

            try (Stream<Path> paths = Files.list(path)) {
                isNotEmpty = paths.findFirst().isPresent();

            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                return false;
            }

            if (isNotEmpty) {
                log.error(cliBundle.getString("log.error.invalidOutputPath"));
                return false;
            }
        }

        return true;
    }

    private boolean checkForNullAndExists(@Nullable Path path, @NotNull String missingMsg, @NotNull String invalidMsg) {
        if (path == null) {
            log.error(missingMsg);
            return false;
        }

        if (!Files.exists(path)) {
            log.error(invalidMsg);
            return false;
        }

        return true;
    }
}
