package de.jsilbereisen.perfumator;

import de.jsilbereisen.perfumator.io.CommandLineHandler;
import de.jsilbereisen.perfumator.io.CommandLineInput;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

/**
 * Entry point of the application when running from the command line.
 */
public class PerfumatorStarter {

    public static void main(String[] args) {
        CommandLineInput cliInput = new CommandLineInput();
        CmdLineParser cliParser = new CmdLineParser(cliInput, ParserProperties.defaults().withUsageWidth(120));
        CommandLineHandler cliHandler = new CommandLineHandler(cliParser);

        try {
            cliParser.parseArgument(args);
        } catch (CmdLineException cliException) {
            cliHandler.handleError(args, cliException);
            System.exit(1);
        }

        cliHandler.handleArguments(cliInput);
    }
}
