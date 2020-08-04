package de.uni_passau.fim.se2.litterbox.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class UnmodifiableListBuilderTest {

    @Test
    public void addTest() {
        UnmodifiableListBuilder<String> builder = UnmodifiableListBuilder.builder();
        builder.add("This").add("is").add("a").add("test");
        List<String> list = builder.build();
        String result = list.stream().collect(Collectors.joining(" "));
        assertEquals("This is a test", result);
    }

    @Test
    public void addFailTest() {
        UnmodifiableListBuilder<String> builder = UnmodifiableListBuilder.builder();
        builder.add("This").add("is").add("a").add("test");
        List<String> list = builder.build();
        try {

            list.add("No can do");
            fail();
        } catch (UnsupportedOperationException e) {
            // is expected
        }
        String result = list.stream().collect(Collectors.joining(" "));
        assertEquals("This is a test", result);
    }
}
