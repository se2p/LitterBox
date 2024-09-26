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
package de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast;

public enum KnownInputs {
    ANGLE("ANGLE"),
    /**
     * X-Axis for mBlocks sometimes used instead of {@link #X_AXIS}.
     */
    AXIS_X("AXIS-X"),
    /**
     * Y-Axis for mBlocks sometimes used instead of {@link #Y_AXIS}.
     */
    AXIS_Y("AXIS-Y"),
    BACKDROP("BACKDROP"),
    BEAT("BEAT"),
    BEATS("BEATS"),
    BRIGHTNESS("BRIGHTNESS"),
    BROADCAST_INPUT("BROADCAST_INPUT"),
    CHANGE("CHANGE"),
    COLOR("COLOR"),
    COLOR2("COLOR2"),
    COLOR_PARAM("COLOR_PARAM"),
    CONDITION("CONDITION"),
    CLONE_OPTION("CLONE_OPTION"),
    COSTUME("COSTUME"),
    CUSTOM_BLOCK("custom_block"),
    DEGREES("DEGREES"),
    DIRECTION("DIRECTION"),
    DRUM("DRUM"),
    DURATION("DURATION"),
    DX("DX"),
    DY("DY"),
    FROM("FROM"),
    HZ("HZ"),
    INDEX("INDEX"),
    INSTRUMENT("INSTRUMENT"),
    ITEM("ITEM"),
    KEY_OPTION("KEY_OPTION"),
    LANGUAGE("LANGUAGE"),
    LED_BLUE("B"),
    LED_GREEN("G"),
    LED_RED("R"),
    LEFT_POWER("LEFT_POWER"),
    LETTER("LETTER"),
    MESSAGE("MESSAGE"),
    NOTE("NOTE"),
    NUM("NUM"),
    NUM1("NUM1"),
    NUM2("NUM2"),
    /**
     * Special input name for mBlocks not using {@link #NUM}.
     */
    NUMBER("NUMBER"),
    /**
     * Special input name for mBlocks not using {@link #NUM1}.
     */
    NUMBER1("NUMBER1"),
    /**
     * Special input name for mBlocks not using {@link #NUM2}.
     */
    NUMBER2("NUMBER2"),
    OBJECT("OBJECT"),
    OPERAND("OPERAND"),
    OPERAND1("OPERAND1"),
    OPERAND2("OPERAND2"),
    POWER("POWER"),
    POWER_LEFT("POWER_LEFT"),
    POWER_RIGHT("POWER_RIGHT"),
    QUESTION("QUESTION"),
    RIGHT_POWER("RIGHT_POWER"),
    SECS("SECS"),
    SIZE("SIZE"),
    SOUNDBEAT("SOUNDBEAT"),
    SOUNDVOLUME("SOUNDVOLUME"),
    SOUND_MENU("SOUND_MENU"),
    STEPS("STEPS"),
    STRING("STRING"),
    STRING1("STRING1"),
    STRING2("STRING2"),
    SUBSTACK("SUBSTACK"),
    SUBSTACK2("SUBSTACK2"),
    TEMPO("TEMPO"),
    TEXT("TEXT"),
    TIME("TIME"),
    /**
     * Repetition for loop block.
     */
    TIMES("TIMES"),
    TO("TO"),
    TOWARDS("TOWARDS"),
    DISTANCE_TO_MENU("DISTANCETOMENU"),
    TOUCHINGOBJECTMENU("TOUCHINGOBJECTMENU"),
    VALUE("VALUE"),
    VOICE("VOICE"),
    VOLUME("VOLUME"),
    WORDS("WORDS"),
    X("X"),
    /**
     * X-Axis for mBlocks sometimes used instead of {@link #AXIS_X}.
     */
    X_AXIS("X"),
    Y("Y"),
    /**
     * Y-Axis for mBlocks sometimes used instead of {@link #AXIS_Y}.
     */
    Y_AXIS("Y"),
    ;

    private final String name;

    KnownInputs(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
