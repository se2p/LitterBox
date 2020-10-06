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
