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

public enum Identifier {

    LEGACY_GREEN_FLAG("whenGreenFlag"),
    LEGACY_KEYPRESS("whenKeyPressed"),
    LEGACY_THIS_CLICKED("whenClicked"),
    LEGACY_BACKDROP("whenSceneStarts"),
    LEGACY_GREATER_THAN("whenSensorGreaterThan"),
    LEGACY_CHANGE_VAR("\"changeVar:by:\"\""),
    LEGACY_START_CLONE("whenCloned"),
    LEGACY_CREATE_CLONE("createCloneOf_myself_"),
    LEGACY_IF("\"doIf"),
    LEGACY_IF_ELSE("\"doIfElse"),
    LEGACY_RECEIVE("whenIReceive"),
    LEGACY_BROADCAST("broadcast:"),
    LEGACY_BROADCAST_WAIT("doBroadcastAndWait"),
    LEGACY_SENSE("getAttribute"),
    LEGACY_FORWARD("\"forward:"),
    LEGACY_CHANGEX("\"changeXposBy:"),
    LEGACY_CHANGEY("\"changeYposBy:"),
    LEGACY_IF_TOUCHING("\"doIf[touching:"),
    LEGACY_IF_COLOR("\"doIf[touchingColor:"),
    LEGACY_IF_KEY("\"doIf[touching:"),
    LEGACY_REPEAT_FALSE("\"doUntilfalse"),
    LEGACY_FOREVER("doForever"),
    LEGACY_FOREVER_IF("doForeverIf"),
    LEGACY_REPEAT("doForLoop"),
    LEGACY_REPEAT_UNTIL("doUntil"),
    LEGACY_CUSTOM_BLOCK("\"procDef\"\""),
    LEGACY_CUSTOM_BLOCK_CALL("call"),
    LEGACY_SETVAR("\"setVar:to:\"\""),
    LEGACY_READ_VAR("readVariable"),
    LEGACY_WAIT("\"wait:elapsed:from:"),
    LEGACY_TURN("turn"),
    LEGACY_CHANGE("change"),
    LEGACY_SAY("say"),
    LEGACY_THINK("think"),
    LEGACY_SHOW("show"),
    LEGACY_HIDE("hide"),
    LEGACY_PLAY("play"),
    LEGACY_PLAY_WAIT("doPlaySoundAndWait"),
    LEGACY_DRUM("drum"),
    LEGACY_HEADING("heading:"),
    LEGACY_POINT("pointTowards:"),
    LEGACY_FRONT("comeToFront"),
    LEGACY_GO("go"),
    LEGACY_GLIDE("glideSecs"),
    WAIT("control_wait"),
    CUSTOM_BLOCK("procedures_definition"),
    CUSTOM_BLOCK_CALL("procedures_call"),
    REPEAT_UNTIL("control_repeat_until"),
    REPEAT("control_repeat"),
    FOREVER("control_forever"),
    SENSE("sensing_of"),
    BACKDROP("event_whenbackdropswitchesto"),
    GREATER_THAN("event_whengreaterthan"),
    KEY_OPTION("KEY_OPTION"),
    TOUCHING_OBJECT("TOUCHINGOBJECTMENU"),
    CLONE_OPTION("CLONE_OPTION"),
    MYSELF("_myself_"),
    THIS_CLICKED("event_whenthisspriteclicked"),
    IF("control_if"),
    IF_ELSE("control_if_else"),
    KEYPRESS("event_whenkeypressed"),
    SENSE_KEYPRESS("sensing_keypressed"),
    SENSE_TOUCHING("sensing_touching"),
    GREEN_FLAG("event_whenflagclicked"),
    BROADCAST("event_broadcast"),
    BROADCAST_WAIT("event_broadcastandwait"),
    RECEIVE("event_whenbroadcastreceived"),
    CREATE_CLONE("control_create_clone_of"),
    START_CLONE("control_start_as_clone"),
    FIELD_VARIABLE("VARIABLE"),
    FIELD_BROADCAST("BROADCAST_INPUT"),
    FIELD_RECEIVE("BROADCAST_OPTION"),
    CHANGE_VAR("data_changevariableby"),
    SET_VAR("data_setvariableto"),
    FORWARD("motion_movesteps"),
    MOTION("motion_"),
    LOOKS("looks_"),
    SOUND("sound_"),
    CHANGE_X("motion_changexby"),
    CHANGE_Y("motion_changeyby"),

    LEGACY_MOTION("motion"),
    LEGACY_LOOKS("looks"),
    LEGACY_DATA("data"),
    LEGACY_AND("and"),
    LEGACY_OR("or"),
    LEGACY_NOT("not"),
    LEGACY_STOP("stop"),
    LEGACY_WAIT_UNTIL("wait_until"),
    LEGACY_ASK_WAIT("ask_and_wait"),
    LEGACY_VIDEO("video"),
    AND("operator_and"),
    OR("operator_or"),
    NOT("operator_not"),
    WAIT_UNTIL("control_wait_until"),
    ASK_WAIT("control_ask_and_wait"),
    STOP("control_stop"),
    VIDEO("videoSensing_"),
    DATA("data_");

    private String value;

    Identifier(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
