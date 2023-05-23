package de.jsilbereisen.perfumator.io;

import de.jsilbereisen.perfumator.engine.EngineConfiguration;
import de.jsilbereisen.perfumator.i18n.Bundles;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineParser;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CommandLineHandlerTest {

    private static final Path PATH_TO_THIS_CLASS = Paths.get("src", "test", "java", "de",
            "jsilbereisen", "perfumator", "io", "CommandLineHandlerTest.java");

    private static final Path PATH_TO_THIS_DIR = Paths.get("src", "test", "java", "de",
            "jsilbereisen", "perfumator", "io");

    @Test
    void singleJavaFileAsInput() {
        CommandLineInput commandLineInput = new CommandLineInput();
        commandLineInput.setPathToSourceDir(PATH_TO_THIS_CLASS);
        commandLineInput.setPathToOutputDir(PATH_TO_THIS_DIR);

        // Mocks
        ResourceBundle mockedBundle = Mockito.mock(ResourceBundle.class);
        when(mockedBundle.getString(anyString())).thenReturn("Mocked");
        CmdLineParser mockedParser = Mockito.mock(CmdLineParser.class);

        CommandLineHandler handler = new CommandLineHandler(mockedParser);

        try (MockedStatic<Bundles> bundles = Mockito.mockStatic(Bundles.class)) {
            bundles.when(Bundles::getCliBundle).thenReturn(mockedBundle);

            assertThat(handler.handleArguments(commandLineInput))
                    .isEqualTo(new EngineConfiguration(PATH_TO_THIS_CLASS, PATH_TO_THIS_DIR));
        }
    }

}