package de.jsilbereisen.perfumator.io;

import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Representation of the available command line options and arguments.
 * Uses the <b>Args4j</b> library (see <a href="https://github.com/kohsuke/args4j">here</a>).
 */
public class CommandLineInput {

    @Option(name = "-i", aliases = { "--input" }, metaVar = "<path>",
            usage = "The directory with with the Java source files to check, p.e. the \"src\""
                    + " folder in a Maven project, or a single Java source file.",
            required = true)
    private Path pathToSourceDir;

    @Option(name = "-o", aliases = { "--output" }, metaVar = "<path>",
            usage = "The directory where the output files should be placed.",
            required = true)
    private Path pathToOutputDir;

    @Option(name = "-h", aliases = { "--help" },
            usage = "Print the usage help.",
            help = true)
    private boolean printHelp;

    @Option(name = "-f", aliases = { "--format" }, metaVar = "JSON | CSV",
            usage = "Specifies the output format. Currently supported (case-insensitive): JSON (default), CSV.")
    private OutputFormat outputFormat; // TODO: test if its case sensitive

    @Option(name = "-l", aliases = { "--language" }, metaVar = "<tag>",
            usage = "Language to prefer for the output. If the specified tag is not a valid language code,"
                    + " no tag is given"
                    + " or one of the output resources is not available for the specified language,"
                    + " English (tag \"EN\") is used as a default fallback.",
            handler = LocaleOptionHandler.class)
    private Locale locale;


}
