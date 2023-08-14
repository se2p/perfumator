package de.jsilbereisen.perfumator.io;

import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

import de.jsilbereisen.perfumator.io.output.OutputFormat;
import org.kohsuke.args4j.spi.MultiPathOptionHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Representation of the available command line options and arguments.
 * Uses the <b>Args4j</b> library (see <a href="https://github.com/kohsuke/args4j">here</a>).
 */
@Getter
@Setter
public class CommandLineInput {

    @Option(name = "-i", aliases = {"--input"}, metaVar = "option.metaVar.path",
            usage = "option.usage.inputDir")
    private Path pathToSourceDir;

    @Option(name = "-o", aliases = {"--output-dir"}, metaVar = "option.metaVar.path",
            usage = "option.usage.outputDir")
    private Path pathToOutputDir;

    @Option(name = "-h", aliases = {"--help"},
            usage = "option.usage.help",
            help = true)
    private boolean printHelp;

    @Option(name = "-f", aliases = {"--format"}, metaVar = "option.metaVar.format",
            usage = "option.usage.format")
    private OutputFormat outputFormat = OutputFormat.getDefault();

    @Option(name = "-l", aliases = {"--language"},
            usage = "option.usage.language",
            handler = LocaleOptionHandler.class)
    private Locale locale = LocaleOptionHandler.getDefault();

    @Option(name = "-d", aliases = {"--dependencies"}, metaVar = "option.metaVar.dependencies",
            usage = "option.usage.dependencies", handler = MultiPathOptionHandler.class)
    private List<Path> dependencies = new ArrayList<>();

    @Option(name = "-b", aliases = {"--batch-size"}, metaVar = "option.metaVar.batchSize",
            usage = "option.usage.batchSize")
    private int batchSize = OutputConfiguration.DEFAULT_BATCH_SIZE;
}
