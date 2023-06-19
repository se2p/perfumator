package de.jsilbereisen.perfumator.util;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class MutablePair<A, B> {

    private A first;

    private B second;

    public MutablePair() { }

    public MutablePair(@Nullable A first, @Nullable B second) {
        this.first = first;
        this.second = second;
    }
}
