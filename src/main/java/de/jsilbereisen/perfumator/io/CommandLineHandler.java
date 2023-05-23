package de.jsilbereisen.perfumator.io;

import de.jsilbereisen.perfumator.engine.EngineConfiguration;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Handles the command line on application start and invokes the appropriate actions, depending on the arguments and
 * options.
 */
@Slf4j
public class CommandLineHandler {

    private final CmdLineParser parser;

    public CommandLineHandler(@NotNull CmdLineParser parser) {
        this.parser = parser;
    }

    // TODO: refactor method
    /**
     * Handles the given application input according to the set options.
     */
    public @Nullable EngineConfiguration handleArguments(@NotNull CommandLineInput cliInput) {
        Locale applicationLocale = cliInput.getLocale();
        String applicationLanguageName = LanguageTag.of(applicationLocale).getFullLanguageName();

        BundlesLoader.loadCliBundle(applicationLocale);
        ResourceBundle cliBundle = Bundles.getCliBundle();
        log.info(cliBundle.getString("log.generic.locale") + " " + applicationLanguageName);

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
            log.error(cliBundle.getString("log.generic.terminate"));
            return null;
        }

        log.info(cliBundle.getString("log.generic.inputPath") + " "
                + inputPath.toAbsolutePath());
        log.info(cliBundle.getString("log.generic.outputPath") + " "
                + outputPath.toAbsolutePath());
        log.info(cliBundle.getString("log.generic.outputFormat") + " "
                + cliInput.getOutputFormat().getAbbreviation());

        return new EngineConfiguration(inputPath, outputPath, applicationLocale, cliInput.getOutputFormat());
    }

    public void handleError(@NotNull String[] args, @NotNull CmdLineException cliException) {
        Locale locale = LocaleOptionHandler.getDefault();

        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-l") || args[i].equals("--language")) && i+1 < args.length) {
                locale = LanguageTag.of(args[i+1]).getRelatedLocale();
                break;
            }
        }

        BundlesLoader.loadCliBundle(locale);
        ResourceBundle cliBundle = Bundles.getCliBundle();

        log.error(cliBundle.getString("log.error.unableToHandleInput"));
        log.error(cliException.getMessage() + "\n");
        printHelp();

        log.error(cliBundle.getString("log.generic.terminate"));
    }

    private void printHelp() {
        ResourceBundle cliBundle = Bundles.getCliBundle();
        log.info(cliBundle.getString("log.generic.preHelp"));
        parser.printUsage(new OutputStreamWriter(System.out), cliBundle);
    }

    private boolean checkInputPath(@Nullable Path path) {
        ResourceBundle cliBundle = Bundles.getCliBundle();

        if (!checkForNullAndExists(path, cliBundle.getString("log.error.inputPathMissing"),
                cliBundle.getString("log.error.invalidInputPath"))) {
            return false;
        }

        assert path != null;

        // TODO: Refactoring: Util class, Method isJavaFile?
        if (!Files.isDirectory(path) && !path.getFileName().toString().endsWith(".java")) {
            log.error(cliBundle.getString("log.error.invalidInputPath"));
            return false;
        }

        return true;
    }

    private boolean checkOutputPath(@Nullable Path path) {
        ResourceBundle cliBundle = Bundles.getCliBundle();

        if (!checkForNullAndExists(path, cliBundle.getString("log.error.outputPathMissing"),
                cliBundle.getString("log.error.invalidOutputPath"))) {
            return false;
        }

        assert path != null;

        if (!Files.isDirectory(path)) {
            log.error(cliBundle.getString("log.error.invalidOutputPath"));
            return false;
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
