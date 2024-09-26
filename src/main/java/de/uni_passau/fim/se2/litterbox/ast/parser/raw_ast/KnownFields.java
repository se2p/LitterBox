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
package de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast;

public enum KnownFields {
    AXIS("AXIS"),
    BACKDROP("BACKDROP"),
    BLACK_WHITE("BLACK_WHITE"),
    BROADCAST_OPTION("BROADCAST_OPTION"),
    BUTTONS("BUTTONS"),
    CLONE_OPTION("CLONE_OPTION"),
    COLOR("COLOR"),
    COLORLIST("COLORLIST"),
    COLOR_PARAM("colorParam"),
    COSTUME("COSTUME"),
    CURRENTMENU("CURRENTMENU"),
    DIRECTION("DIRECTION"),
    DISTANCE_TO_MENU("DISTANCETOMENU"),
    DRAG_MODE("DRAG_MODE"),
    DRUM("DRUM"),
    EFFECT("EFFECT"),
    FACE_PANEL("FACE_PANEL"),
    FORWARD_BACKWARD("FORWARD_BACKWARD"),
    FRONT_BACK("FRONT_BACK"),
    INSTRUMENT("INSTRUMENT"),
    IS_PRESSED("IS_PRESS"),
    KEY_OPTION("KEY_OPTION"),
    LANGUAGES("languages"),
    /**
     * mBlock LED position.
     *
     * <p>Note: The typo in the value is intentional. The programs have a field named like this.
     */
    LED_POSITION("LED_POSTION"),
    LINE_FOLLOW_STATE("LINEFOLLOW_STATE"),
    LIST("LIST"),
    MENU_LIST("MENU_LIST"),
    MOVE_DIRECTION("MOVE_DIRECTION"),
    NOTE("NOTE"),
    NUMBER_NAME("NUMBER_NAME"),
    OBJECT("OBJECT"),
    OPERATOR("OPERATOR"),
    OPTION("OPTION"),
    ORIENTATE("ORIENTATE"),
    PANEL("PANEL"),
    PORT("PORT"),
    PROPERTY("PROPERTY"),
    REMOTE_KEY("REMOTE_KEY"),
    RGB("RGB"),
    SOUNDLIST("SOUNDLIST"),
    SOUNDNOTE("SOUNDNOTE"),
    SOUND_MENU("SOUND_MENU"),
    STOP_OPTION("STOP_OPTION"),
    STYLE("STYLE"),
    TO("TO"),
    TOUCHINGOBJECTMENU("TOUCHINGOBJECTMENU"),
    TOWARDS("TOWARDS"),
    VALUE("VALUE"),
    VARIABLE("VARIABLE"),
    VOICES("voices"),
    WHEN_GREATER_THAN_MENU("WHENGREATERTHANMENU");

    private final String name;

    KnownFields(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
