/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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
