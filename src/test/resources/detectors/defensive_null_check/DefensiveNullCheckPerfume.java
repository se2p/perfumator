package de.jsilbereisen.test;

/**
 * Test detection of the "Defensive Null Check" Perfume. The goal of this Perfume is to reward
 * checking the non-primitive parameters of public methods for {@code null} when they are not annotated with a {@code @NotNull},
 * {@code @Nonnull} or {@code @Nullable} Annotation. Also, it rewards usage of these Annotations, as
 * they give direct signals on how the API handles {@code null} values to other developers.
 */
public class DefensiveNullCheckPerfume {

    // Should be detected
    public void methodWithNullChecks(int nonNullNumber, Object a, String b) {
        if (a == null || b == null) {
            doSomething();
            mightThrowExceptionOrWhatEver();
        }
    }

    // Should be detected
    public void methodWithNullChecksMultipleIfs(int nonNullNumber, Object a, String b) {
        if (a == null) {
            throw new SomeException();
        }
        if (null == b) {
            throw new AnotherException();
        }
    }

    // Should be detected
    public void methodWithNotEmqualsNullCheck(int nonNullNumber, Object a, String b) {
        if (a == null) {
            throw new IllegalArgumentException();
        } else if (b != null) {
            action();
        }

        doSomething();
    }

    // Should be detected, the API clearly signals the handling of null-values for all Params through annotations
    public void methodWithAnnotations(int nonNullNumber, @Nullable Object a, @NotNull String b,
                                      @Nonnull List<String> l, @NonNull Integer i) {
        doSomething();
    }

    // Should be detected, all Params are either: primitive, annotated or checked upon
    public void methodWithMixedParams(int x, @Nullable Object y, String b, @NotNull @SomeAnno List<String> l) {
        if (!(null == b)) {
            doSomething();
        } else {
            throw new IllegalArgumentException();
        }
    }

    interface X {
        // Should be detected, has default implementation
        default int defaultMethod(@Nullable Object o) {
            doSomething();
        }
    }

    // TODO: Consider Objects.requireNonNull ?
}