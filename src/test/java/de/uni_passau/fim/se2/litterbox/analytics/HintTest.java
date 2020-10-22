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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptySprite;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class HintTest {

    @Test
    public void testParameterReplacement() {
        Hint hint = new Hint(EmptySprite.NAME);
        assertTrue(hint.hasParameters());
        assertEquals(1, hint.getNumParameters());

        String origText = hint.getHintText();
        assertThat(origText.indexOf(Hint.ESCAPE_CHARACTER)).isAtLeast(0);

        hint.setParameter(Hint.HINT_SPRITE, "FooBar");
        assertFalse(hint.hasParameters());
        assertEquals(0, hint.getNumParameters());
        assertEquals(origText.replace(""+Hint.ESCAPE_CHARACTER+Hint.HINT_SPRITE, "FooBar"), hint.getHintText());
    }
}
