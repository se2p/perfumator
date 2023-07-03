package de.jsilbereisen.test;

public class HasPerfumes4 {

    // Example for equals blueprint
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof HasPerfumes4 p)) {
            return false;
        }

        return true;
    }
}