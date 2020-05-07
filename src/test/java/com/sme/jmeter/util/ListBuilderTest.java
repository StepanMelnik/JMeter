package com.sme.jmeter.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests of {@link ListBuilder}.
 */
public class ListBuilderTest
{
    @Test
    public void testListBuilder() throws Exception
    {
        List<String> list = new ListBuilder<>("One", "Two")
                .add("Three")
                .build();

        assertEquals(Arrays.asList("One", "Two", "Three"), list);
    }
}
