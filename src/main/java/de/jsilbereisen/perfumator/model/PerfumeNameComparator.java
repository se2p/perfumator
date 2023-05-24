package de.jsilbereisen.perfumator.model;

import java.util.Comparator;

public class PerfumeNameComparator implements Comparator<Perfume> {

    @Override
    public int compare(Perfume o1, Perfume o2) {
        if (o1.getName() == null) {
            if (o2.getName() == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o2.getName() == null) {
            return 1;
        }

        return o1.getName().compareTo(o2.getName());
    }
}
