package io;

import de.jsilbereisen.perfumator.engine.EngineConfiguration;
import de.jsilbereisen.perfumator.i18n.Bundles;
import de.jsilbereisen.perfumator.io.CommandLineHandler;
import de.jsilbereisen.perfumator.io.CommandLineInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineParser;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CommandLineHandlerTest {

    private static final Path PATH_TO_THIS_CLASS = Paths.get("src", "test", "java", "io",
            "CommandLineHandlerTest.java");

    private static final Path PATH_TO_THIS_DIR = Paths.get("src", "test", "java", "io");

    private static final Bundles MOCKED_BUNDLES = Mockito.mock(Bundles.class);

    private static final CmdLineParser MOCKED_PARSER = Mockito.mock(CmdLineParser.class);

    @BeforeAll
    static void setupBundlesMock() {
        ResourceBundle mockedBundle = Mockito.mock(ResourceBundle.class);
        when(mockedBundle.getString(anyString())).thenReturn("Mocked");
        when(MOCKED_BUNDLES.getCliBundle()).thenReturn(mockedBundle);
    }

    @Test
    void singleJavaFileAsInput() {
        CommandLineInput commandLineInput = new CommandLineInput();
        commandLineInput.setPathToSourceDir(PATH_TO_THIS_CLASS);
        commandLineInput.setPathToOutputDir(PATH_TO_THIS_DIR);

        CommandLineHandler handler = new CommandLineHandler(MOCKED_PARSER, MOCKED_BUNDLES);

        assertThat(handler.handleArguments(commandLineInput))
                .isEqualTo(new EngineConfiguration(PATH_TO_THIS_CLASS, PATH_TO_THIS_DIR));
    }

}