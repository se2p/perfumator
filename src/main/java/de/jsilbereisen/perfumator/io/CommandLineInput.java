package de.jsilbereisen.perfumator.io;

import lombok.Getter;
import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Representation of the available command line options and arguments.
 * Uses the <b>Args4j</b> library (see <a href="https://github.com/kohsuke/args4j">here</a>).
 */
@Getter
public class CommandLineInput {

    @Option(name = "-i", aliases = { "--input-dir" }, metaVar = "option.metaVar.path",
            usage = "option.usage.inputDir")
    private Path pathToSourceDir;

    @Option(name = "-o", aliases = { "--output-dir" }, metaVar = "option.metaVar.path",
            usage = "option.usage.outputDir")
    private Path pathToOutputDir;

    @Option(name = "-h", aliases = { "--help" },
            usage = "option.usage.help",
            help = true)
    private boolean printHelp;

    @Option(name = "-f", aliases = { "--format" }, metaVar = "option.metaVar.format",
            usage = "option.usage.format")
    private OutputFormat outputFormat = OutputFormat.getDefault(); // TODO: test if its case sensitive

    @Option(name = "-l", aliases = { "--language" },
            usage = "option.usage.language",
            handler = LocaleOptionHandler.class)
    private Locale locale = LocaleOptionHandler.getDefault();

    /**
     * Creates a copy of {@code this}.
     */
    public CommandLineInput copy() {
        CommandLineInput copy = new CommandLineInput();

        copy.pathToSourceDir = pathToSourceDir; // Path is immutable
        copy.pathToOutputDir = pathToOutputDir;
        copy.printHelp = printHelp;
        copy.outputFormat = outputFormat;
        copy.locale = (Locale) locale.clone(); // Not sure if Locale is immutable - not mentioned in the class doc

        return copy;
    }
}
