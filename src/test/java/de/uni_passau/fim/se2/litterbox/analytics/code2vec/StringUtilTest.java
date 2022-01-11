package de.uni_passau.fim.se2.litterbox.analytics.code2vec;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void testNormalizeName() {
        assertEquals("testone", StringUtil.normalizeName("test ONE"));
        assertEquals("testtwo", StringUtil.normalizeName("test\ntwo"));
        assertEquals("testthree", StringUtil.normalizeName("test,\"three'"));
        assertEquals("testfour", StringUtil.normalizeName("t̷̡̢̛̛̫͙̠̻̣̮̩̗͔͕̙̂̿̆͐͒̋́̓̄͌̆̒̏̈͛̈̋̉̈̋̓̕ȩ̷̨̛̛̮͕̭͖̠͖̦̹͚̖̦̥͙̪̳͙̘͉̋̿̾͊̆͊͗̒́̀́͊͗̓̊̉̕͘͜s̶̨̺̘̬̖͆͊͛̂͐̂̔̐̍̈̾̉̅̓͊͒̾̚͜͝͝͝͝͝ţ̴̧͇̻̬̰̬̹̙̼͕͇̭̖̫̜̠̗͔̣̜͎͍̠̙̳͔̝͗̑͛̔̈̀́̊͝͠ͅ four"));
    }

    @Test
    void testSplitToSubtokens() {
        ArrayList<String> subTokensTest1 = StringUtil.splitToSubtokens("ForeverAlone");
        assertEquals(2, subTokensTest1.size());
        assertEquals("forever", subTokensTest1.get(0));
        assertEquals("alone", subTokensTest1.get(1));
        ArrayList<String> subTokensTest2 = StringUtil.splitToSubtokens("Forever   _ Alone-3\ninThis98World?! ");
        assertEquals(5, subTokensTest2.size());
        assertEquals("forever", subTokensTest2.get(0));
        assertEquals("alone", subTokensTest2.get(1));
        assertEquals("in", subTokensTest2.get(2));
        assertEquals("this", subTokensTest2.get(3));
        assertEquals("world", subTokensTest2.get(4));
    }
}