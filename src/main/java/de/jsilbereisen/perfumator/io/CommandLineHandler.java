package de.jsilbereisen.perfumator.io;

import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.i18n.BundlesLoader;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Handles the command line on application start and invokes the appropriate actions, depending on the arguments and
 * options.
 */
@Slf4j
public class CommandLineHandler {

    private final CmdLineParser parser;

    public CommandLineHandler(CmdLineParser parser) {
        this.parser = parser;
    }

    /**
     * Handles the given application input according to the set options.
     */
    public void handleArguments(CommandLineInput cliInput) {
        BundlesLoader.loadCliBundle(cliInput.getLocale());

        if (cliInput.isPrintHelp()) {
            printHelp();
        }

        log.info(cliInput.toString());
        // TODO: Log the read values
        // TODO: Check Paths (util class?), start application logic (own class)
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

        System.err.println(cliBundle.getString("out.error.errorPrompt") + " "
                + cliBundle.getString("out.error.unableToHandleInput"));
        System.err.println(cliBundle.getString("out.error.errorPrompt") + " "
                + cliException.getMessage() + "\n");
        printHelp();
    }

    private void printHelp() {
        ResourceBundle cliBundle = Bundles.getCliBundle();
        System.out.println(cliBundle.getString("out.generic.preHelp"));
        parser.printUsage(new OutputStreamWriter(System.out), cliBundle);
    }
}
