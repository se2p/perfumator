package de.jsilbereisen.perfumator.model;

import java.util.Comparator;

// TODO: tests
public class DetectedInstanceComparator<T extends Detectable> implements Comparator<DetectedInstance<T>> {

    @Override
    public int compare(DetectedInstance<T> o1, DetectedInstance<T> o2) {
        // trivial comparison
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }
        if (o2 == null) {
            return 1;
        }

        // compare first by Detectable
        int detectableComparisonResult = 0;
        if (o1.getDetectable() != null) {
            detectableComparisonResult = o1.getDetectable().compareTo(o2.getDetectable());
        } else {
            detectableComparisonResult = o2.getDetectable() == null ? 0 : -1;
        }
        if (detectableComparisonResult != 0) {
            return detectableComparisonResult;
        }

        // compare by type name
        int typeNameComparisonResult = 0;
        if (o1.getTypeName() != null) {
            typeNameComparisonResult = o1.getTypeName().compareTo(o2.getTypeName());
        } else {
            typeNameComparisonResult = o2.getTypeName() == null ? 0 : -1;
        }
        if (typeNameComparisonResult != 0) {
            return typeNameComparisonResult;
        }

        // compare by begin line numbers
        int beginLineNumberComparisonResult = o2.getBeginningLineNumber() - o1.getBeginningLineNumber();
        if (beginLineNumberComparisonResult != 0) {
            return beginLineNumberComparisonResult;
        }

        // final comparison on ending line numbers
        return o2.getEndingLineNumber() - o1.getEndingLineNumber();
    }
}
