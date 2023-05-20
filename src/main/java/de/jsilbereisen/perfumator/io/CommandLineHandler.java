package de.jsilbereisen.perfumator.io;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

// TODO: i18n
/**
 * Handles the command line on application start and invokes the appropriate actions, depending on the arguments and
 * options.
 */
@Slf4j
public class CommandLineHandler {

    private final CmdLineParser parser;

    public CommandLineHandler(CmdLineParser parser) { // TODO: Docs
        this.parser = parser;
    }

    public void handleArguments(CommandLineInput cliInput) {
        if (cliInput.isPrintHelp()) {
            printHelp();
        }

        // TODO: Log the read values
        // TODO: Check Paths (util class?), start application logic (own class)
    }

    public void printHelp() {
        parser.printUsage(System.out);
    }

    public void handleError(CmdLineException exception) {
        log.error("Unable to handle command line.");
        log.error(exception.getMessage() + "\n");
        parser.printUsage(System.out);
    }
}
