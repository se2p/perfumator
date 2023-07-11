package util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import de.jsilbereisen.perfumator.util.StringUtil;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for utility methods for {@link String}s.
 */
class StringUtilTest {

    @Test
    void isEmpty() {
        // Happy Paths
        Assertions.assertThat(StringUtil.isEmpty(null)).isTrue();
        assertThat(StringUtil.isEmpty("")).isTrue();
        assertThat(StringUtil.isEmpty(" \n\t")).isTrue();

        // Unhappy Paths
        assertThat(StringUtil.isEmpty("hallo")).isFalse();
        assertThat(StringUtil.isEmpty("    \nHi")).isFalse();
    }

    @Test
    void anyEmpty() {
        // Happy Paths
        assertThat(StringUtil.anyEmpty("Hallo", "Welt", " \n  \t    ")).isTrue();
        assertThat(StringUtil.anyEmpty(List.of("Hallo", "Welt", " \n \t   "))).isTrue();

        // Unhappy Paths
        assertThat(StringUtil.anyEmpty("Hallo", "Welt", " x ")).isFalse();
        assertThat(StringUtil.anyEmpty(List.of("Hallo", "Welt", " x "))).isFalse();
        assertThat(StringUtil.anyEmpty()).isFalse();
        assertThat(StringUtil.anyEmpty(Collections.emptyList())).isFalse();
    }

    @Test
    void joinStrings() {
        assertThat(StringUtil.joinStrings(List.of("Hallo", "Welt", "ich", "heiße", "..."), " "))
                .isEqualTo("Hallo Welt ich heiße ...");
        assertThat(StringUtil.joinStrings(List.of("Hallo"), ";;")).isEqualTo("Hallo");
        assertThat(StringUtil.joinStrings(Collections.emptyList(), ";;")).isEmpty();
    }
}