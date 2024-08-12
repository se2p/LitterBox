/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.*;

@Isolated  // ensure no parallel tests to avoid parallel mutation of global state (i.e. IssueTranslator language)
class IssueTranslatorTest {

    @AfterEach
    void reset() {
        IssueTranslator.getInstance().setLanguage("en");
    }

    @Test
    void fallbackToEnglish() {
        IssueTranslator.getInstance().setLanguage("no");
        assertEquals("costume", getInfo(IssueTranslator.GeneralTerm.COSTUME));
    }

    @Test
    void fallbackToEnglishMissingLocale() {
        IssueTranslator.getInstance().setLanguage(null);
        assertEquals("attribute", getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE));
    }

    @Test
    void switchLanguage() {
        IssueTranslator.getInstance().setLanguage("en");
        assertEquals("Visibility", getInfo(IssueTranslator.GeneralTerm.VISIBILITY));
        IssueTranslator.getInstance().setLanguage("de");
        assertEquals("Sichtbarkeit", getInfo(IssueTranslator.GeneralTerm.VISIBILITY));
    }

    private String getInfo(final IssueTranslator.GeneralTerm term) {
        return IssueTranslator.getInstance().getInfo(term);
    }
}
