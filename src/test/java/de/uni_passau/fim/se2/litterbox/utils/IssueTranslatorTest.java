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

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class IssueTranslatorTest {

    @Test
    void fallbackToEnglish() {
        final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.of("no"));
        assertEquals("costume", translator.getInfo(IssueTranslator.GeneralTerm.COSTUME));
    }

    @Test
    void fallbackToEnglishMissingLocale() {
        final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator();
        assertEquals("attribute", translator.getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE));
    }

    @Test
    void switchLanguage() {
        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);
        assertEquals("Visibility", translator.getInfo(IssueTranslator.GeneralTerm.VISIBILITY));
        translator = IssueTranslatorFactory.getIssueTranslator(Locale.GERMAN);
        assertEquals("Sichtbarkeit", translator.getInfo(IssueTranslator.GeneralTerm.VISIBILITY));
    }
}
