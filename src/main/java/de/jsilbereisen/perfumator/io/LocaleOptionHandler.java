package de.jsilbereisen.perfumator.io;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import de.jsilbereisen.perfumator.util.StringUtil;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * An {@link OptionHandler} to handle the language tag option.
 * For the available language tags and the default value, see the {@link LanguageTag} class.
 */
public class LocaleOptionHandler extends OptionHandler<Locale> {

    public LocaleOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Locale> setter) {
        super(parser, option, setter);
    }

    /**
     * Returns the default {@link Locale} for the application, determined by the default {@link LanguageTag}.
     */
    public static Locale getDefault() {
        return LanguageTag.getDefault().getRelatedLocale();
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        String languageCode = params.getParameter(0);

        Locale locale = LanguageTag.of(languageCode).getRelatedLocale();
        setter.addValue(locale);

        return 1;
    }

    /**
     * Give a list of all available {@link LanguageTag}s as the Meta Variable.
     */
    @Override
    public String getDefaultMetaVariable() {
        return "[" + StringUtil.joinStrings(LanguageTag.getAllTagNames(), " | ") + "]";
    }

    /**
     * Has to be overriden, in order to not interpret the Meta Variable as a key to a resource bundle
     * when the {@link CommandLineHandler} prints the usage-page with a {@link ResourceBundle}.
     */
    @Override
    public String getMetaVariable(ResourceBundle rb) {
        return getDefaultMetaVariable();
    }
}
