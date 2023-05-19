package de.jsilbereisen.perfumator.io;

import de.jsilbereisen.perfumator.util.StringUtil;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.util.Locale;

/**
 * An {@link OptionHandler} to handle the language tag option.
 * For the available language tags and the default value, see the {@link LanguageTag} class.
 */
public class LocaleOptionHandler extends OptionHandler<Locale> {

    protected LocaleOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Locale> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        String languageCode = params.getParameter(0);

        Locale locale = LanguageTag.of(languageCode).getRelatedLocale();
        setter.addValue(locale);

        return 1;
    }

    @Override
    public String getDefaultMetaVariable() {
        return StringUtil.joinStrings(LanguageTag.getAllTagNames(), " | ");
    }
}
