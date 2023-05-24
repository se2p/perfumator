package de.jsilbereisen.perfumator;

import de.jsilbereisen.perfumator.engine.EngineConfiguration;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.io.CommandLineHandler;
import de.jsilbereisen.perfumator.io.CommandLineInput;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

/**
 * Entry point of the application when running from the command line.
 */
public class CommandLineStarter {

    public static void main(String[] args) {
        CommandLineInput cliInput = new CommandLineInput();
        CmdLineParser cliParser = new CmdLineParser(cliInput,
                ParserProperties.defaults().withUsageWidth(120).withShowDefaults(false));
        CommandLineHandler cliHandler = new CommandLineHandler(cliParser, new Bundles());

        try {
            cliParser.parseArgument(args);
        } catch (CmdLineException cliException) {
            cliHandler.handleError(args, cliException);
            System.exit(1);
        }

        EngineConfiguration config = cliHandler.handleArguments(cliInput);

        // TODO
    }
}
