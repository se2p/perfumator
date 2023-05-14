package de.jsilbereisen.perfumator.data.sources;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SolutionPattern extends PerfumeSource {

    private URL urlToRelatedPattern;

    private RelatedPatternType relatedPatternType;

    public SolutionPattern() {
        super("Solution pattern");
    }

    public SolutionPattern(@Nullable URL urlToRelatedPattern,
                           @Nullable RelatedPatternType relatedPatternType) {
        super("Solution pattern"); // TODO: i18n

        this.urlToRelatedPattern = urlToRelatedPattern;
        this.relatedPatternType = relatedPatternType;
    }

    @Override
    public String toString() {
        String ret = getSourceName();

        if (relatedPatternType != null) {
            ret += "\n" + "Inferred from " + relatedPatternType;
        }
        if (urlToRelatedPattern != null) {
            ret += " (see: " + urlToRelatedPattern + ")";
        }

        return ret;
    }

    // TODO: i18n
}
