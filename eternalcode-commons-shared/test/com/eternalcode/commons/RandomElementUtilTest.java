package com.eternalcode.commons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RandomElementUtilTest {

    @Test
    void testRandomElementWhenCollectionIsEmpty() {
        List<String> emptyList = new ArrayList<>();
        Optional<String> result = RandomElementUtil.randomElement(emptyList);

        assertTrue(result.isEmpty());
    }

    @Test
    void testRandomElementWhenCollectionHasOneElement() {
        List<String> singleItemList = Arrays.asList("One");
        Optional<String> result = RandomElementUtil.randomElement(singleItemList);

        assertTrue(result.isPresent());
        assertEquals("One", result.get());
    }

    @Test
    void testRandomElementWhenCollectionHasMultipleElements() {
        List<String> multiItemList = Arrays.asList("One", "Two", "Three");
        Optional<String> result = RandomElementUtil.randomElement(multiItemList);

        assertTrue(result.isPresent());
        assertTrue(multiItemList.contains(result.get()));
    }
}
