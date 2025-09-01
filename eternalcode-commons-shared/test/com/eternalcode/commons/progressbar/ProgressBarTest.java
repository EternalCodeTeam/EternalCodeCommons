package com.eternalcode.commons.progressbar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgressBarTest {

    @Test
    void testRenderFullProgress() {
        ProgressBar bar = ProgressBar.builder()
            .length(5)
            .build();
        String rendered = bar.render(1.0);

        assertEquals("[█████]", rendered);
    }

    @Test
    void testRenderEmptyProgress() {
        ProgressBar bar = ProgressBar.builder()
            .length(5)
            .build();
        String rendered = bar.render(0.0);

        assertEquals("[░░░░░]", rendered);
    }

    @Test
    void testRenderHalfProgress() {
        ProgressBar bar = ProgressBar.builder()
            .length(4)
            .build();
        String rendered = bar.render(0.5);

        assertEquals("[██░░]", rendered);
    }

    @Test
    void testRenderIntOverMax() {
        ProgressBar bar = ProgressBar.builder()
            .length(3)
            .build();
        String rendered = bar.render(5, 3);

        assertEquals("[███]", rendered);
    }

    @Test
    void testRenderIntWithZeroMax() {
        ProgressBar bar = ProgressBar.builder()
            .length(3)
            .build();
        String rendered = bar.render(0, 0);

        assertEquals("[███]", rendered);
    }

    @Test
    void testHideBrackets() {
        ProgressBar bar = ProgressBar.builder()
            .length(3)
            .hideBrackets()
            .build();
        String rendered = bar.render(1.0);

        assertEquals("███", rendered);
    }

    @Test
    void testCustomCharacters() {
        ProgressBar bar = ProgressBar.builder()
            .length(4)
            .filledChar("#")
            .emptyChar("-")
            .brackets("{", "}")
            .build();

        String rendered = bar.render(0.5);
        assertEquals("{##--}", rendered);
    }

    @Test
    void testNegativeProgressClampedToZero() {
        ProgressBar bar = ProgressBar.builder()
            .length(3)
            .build();
        String rendered = bar.render(-1.0);

        assertEquals("[░░░]", rendered);
    }

    @Test
    void testProgressGreaterThanOneClampedToOne() {
        ProgressBar bar = ProgressBar.builder()
            .length(3)
            .build();
        String rendered = bar.render(2.0);

        assertEquals("[███]", rendered);
    }

    @Test
    void testInvalidLengthThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ProgressBar.builder().length(0));
        assertThrows(IllegalArgumentException.class, () -> ProgressBar.builder().length(-5));
    }
}
