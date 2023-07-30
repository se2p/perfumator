package dependency;

import org.jetbrains.annotations.NotNull;

public class SomeComparable implements Comparable<SomeComparable> {

    @Override
    public int compareTo(@NotNull SomeComparable o) {
        return 0;
    }
}