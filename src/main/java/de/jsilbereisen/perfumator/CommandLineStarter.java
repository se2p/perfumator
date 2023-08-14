package de.jsilbereisen.perfumator;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import de.jsilbereisen.perfumator.engine.DetectionEngine;
import de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.io.CommandLineHandler;
import de.jsilbereisen.perfumator.io.CommandLineInput;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.model.EngineConfiguration;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

/**
 * Entry point of the application when running from the command line.
 */
@Slf4j
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
        if (config == null) {
            return;
        }

        PerfumeDetectionEngine.Builder engineBuilder = PerfumeDetectionEngine.builder(config.getResourcesLocale())
                .setDependencies(config.getDependencies());

        DetectionEngine<Perfume> engine = engineBuilder.build();
        OutputConfiguration outputConfiguration =
                OutputConfiguration.from(config.getOutputDir()).setBatchSize(config.getBatchSize());

        engine.detectAndSerialize(config.getSourcesPath(), outputConfiguration, config.getOutputFormat());
    }
}
