package util;

import de.jsilbereisen.perfumator.model.*;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.model.perfume.RelatedPattern;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComparatorsAndComparablesTest {

    private static final DetectableComparator<Perfume> PERFUME_COMPARATOR = new DetectableComparator<>();

    private static final DetectedInstanceComparator<Perfume> DETECTED_PERFUME_COMPARATOR = new DetectedInstanceComparator<>();

    @Test
    void comparePerfumesTrivial() {
        // Trivial comparisons
        Perfume p1 = null, p2 = null;
        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isZero();

        p1 = new Perfume();
        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isPositive();

        p1 = null;
        p2 = new Perfume();
        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isNegative();
    }

    @Test
    void comparePerfumesSame() {
        Perfume p1 = new Perfume();
        p1.setName("Perfume");
        p1.setRelatedPattern(RelatedPattern.ERROR);

        Perfume p2 = new Perfume();
        p2.setName("Perfume");
        p2.setRelatedPattern(RelatedPattern.ERROR);

        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isZero();

        // Other fields are irrelevant to the comparison
        p1.setDescription("bli");
        p2.setDescription("bla");

        p1.setSource("blub");
        p2.setSource("blob");

        p1.setAdditionalInformation("...");
        p2.setAdditionalInformation(":::");

        p1.setDetectorClassSimpleName("---");
        p2.setDetectorClassSimpleName("___");

        p1.setI18nBaseBundleName("***");
        p2.setI18nBaseBundleName("+++");

        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isZero();
    }

    @Test
    void compareDifferentPerfumes() {
        Perfume p1 = new Perfume();
        p1.setName("A");
        p1.setRelatedPattern(RelatedPattern.STYLE);

        Perfume p2 = new Perfume();
        p2.setName("B");
        p2.setRelatedPattern(RelatedPattern.ERROR);

        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isPositive();

        p1.setRelatedPattern(RelatedPattern.ERROR);
        assertThat(PERFUME_COMPARATOR.compare(p1, p2)).isNegative();
    }

    @Test
    void compareDetectedInstancesTrivial() {
        // Trivial comparisons
        DetectedInstance<Perfume> p1 = null, p2 = null;
        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isZero();

        p1 = new DetectedInstance<>();
        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isPositive();

        p1 = null;
        p2 = new DetectedInstance<>();
        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isNegative();
    }

    @Test
    void compareDetectedInstanceSame() {
        // Same detectable should give 0
        DetectedInstance<Perfume> p1 = new DetectedInstance<>();
        p1.setDetectable(new Perfume());

        DetectedInstance<Perfume> p2 = new DetectedInstance<>();
        p2.setDetectable(new Perfume());

        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isZero();

        // Fields of the DetectedInstance itself
        p1.setTypeName("SomeClass");
        p1.setBeginningLineNumber(0);
        p1.setEndingLineNumber(1);

        p2.setTypeName("SomeClass");
        p2.setBeginningLineNumber(0);
        p2.setEndingLineNumber(1);

        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isZero();

        // Other fields are irrelevant to the comparison
        p1.setConcreteCode("bli");
        p2.setConcreteCode("bla");

        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isZero();
    }

    @Test
    void compareDifferentDetectedInstances() {
        DetectedInstance<Perfume> p1 = new DetectedInstance<>();
        p1.setDetectable(new Perfume());
        p1.setTypeName("ClassA");

        DetectedInstance<Perfume> p2 = new DetectedInstance<>();
        p2.setDetectable(new Perfume());
        p2.setTypeName("ClassB");

        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isNegative();

        p2.setTypeName("ClassA");
        p1.setBeginningLineNumber(0);
        p2.setBeginningLineNumber(10);
        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isNegative();

        p2.setBeginningLineNumber(0);
        p1.setEndingLineNumber(0);
        p2.setEndingLineNumber(10);
        assertThat(DETECTED_PERFUME_COMPARATOR.compare(p1, p2)).isNegative();
    }
}