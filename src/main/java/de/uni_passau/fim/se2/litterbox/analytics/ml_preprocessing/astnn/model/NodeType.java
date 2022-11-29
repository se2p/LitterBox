/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.astnn.model;

public enum NodeType {
    // misc
    UNKNOWN,

    DATA_ITEMNUMOFLIST,
    DATA_ITEMOFLIST,
    DATA_LENGTHOFLIST,
    DATA_LIST,
    DATA_LISTCONTAINSITEM,
    DATA_VARIABLE,

    EMPTY_BOOL,
    EMPTY_NAME,
    EMPTY_NUMBER,
    EMPTY_STRING,

    EVENT_MESSAGE,

    LOOKS_BACKDROPNUMBERNAME,
    LOOKS_COSTUMENUMBERNAME,
    LOOKS_NEXTBACKDROP,
    LOOKS_PREVBACKDROP,
    LOOKS_RANDOMBACKDROP,
    LOOKS_SIZE,

    MOTION_DIRECTION,
    MOTION_XPOSITION,
    MOTION_YPOSITION,
    MOTION_RANDOMPOSITION,
    MOTION_MOUSEPOINTER,

    OPERATOR_ADD,
    OPERATOR_AND,
    OPERATOR_CONTAINS,
    OPERATOR_DIVIDE,
    OPERATOR_EQUALS,
    OPERATOR_GT,
    OPERATOR_JOIN,
    OPERATOR_LENGTH,
    OPERATOR_LETTER_OF,
    OPERATOR_LT,
    OPERATOR_MATHOP,
    OPERATOR_MOD,
    OPERATOR_MULTIPLY,
    OPERATOR_NOT,
    OPERATOR_OR,
    OPERATOR_RANDOM,
    OPERATOR_ROUND,
    OPERATOR_SUBTRACT,

    PROCEDURE_PARAMETER,

    SENSING_ANSWER,
    SENSING_COLORISTOUCHINGCOLOR,
    SENSING_CURRENT,
    SENSING_DAYSSINCE2000,
    SENSING_DISTANCETO,
    SENSING_EDGE,
    SENSING_KEY,
    SENSING_KEYPRESSED,
    SENSING_LOUDNESS,
    SENSING_MOUSEDOWN,
    SENSING_MOUSEX,
    SENSING_MOUSEY,
    SENSING_OF,
    SENSING_TIMER,
    SENSING_TOUCHINGCOLOR,
    SENSING_TOUCHINGOBJECT,
    SENSING_USERNAME,

    SOUND_VOLUME,

    TTS_LANGUAGE,
    TTS_VOICE,

    MUSIC_DRUM,
    MUSIC_INSTRUMENT,
    MUSIC_TEMPO
}
