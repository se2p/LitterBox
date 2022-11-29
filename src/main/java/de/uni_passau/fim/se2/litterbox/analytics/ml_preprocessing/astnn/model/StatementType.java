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

/**
 * The various types of block and regular statements that can occur in the Java language.
 */
public enum StatementType {
    // misc
    ACTOR,
    PROCEDURES_CALL,
    PROCEDURES_DEFINITION,
    PROGRAM,
    SCRIPT,
    UNKNOWN_STMT,

    // blocks
    CONTROL_ELSE,
    CONTROL_FOREVER,
    CONTROL_IF,
    CONTROL_REPEAT,
    CONTROL_REPEAT_UNTIL,
    CONTROL_REPEAT_TIMES,

    // regular statements
    CONTROL_CREATE_CLONE_OF,
    CONTROL_DELETE_THIS_CLONE,
    CONTROL_START_AS_CLONE,
    CONTROL_STOP,
    CONTROL_WAIT,
    CONTROL_WAIT_UNTIL,

    DATA_ADDTOLIST,
    DATA_CHANGEVARIABLEBY,
    DATA_DELETEALLOFLIST,
    DATA_DELETEOFLIST,
    DATA_HIDELIST,
    DATA_HIDEVARIABLE,
    DATA_INSERTATLIST,
    DATA_REPLACEITEMOFLIST,
    DATA_SETVARIABLETO,
    DATA_SHOWLIST,
    DATA_SHOWVARIABLE,

    EVENT_BROADCAST,
    EVENT_BROADCASTANDWAIT,
    EVENT_NEVER,
    EVENT_WHENBACKDROPSWITCHESTO,
    EVENT_WHENBROADCASTRECEIVED,
    EVENT_WHENFLAGCLICKED,
    EVENT_WHENGREATERTHAN,
    EVENT_WHENKEYPRESSED,
    EVENT_WHENSTAGECLICKED,
    EVENT_WHENTHISSPRITECLICKED,

    LOOKS_CHANGEEFFECTBY,
    LOOKS_CHANGESIZEBY,
    LOOKS_CLEARGRAPHICEFFECTS,
    LOOKS_GOFORWARDBACKWARDLAYERS,
    LOOKS_GOTOFRONTBACK,
    LOOKS_HIDE,
    LOOKS_NEXTBACKDROP,
    LOOKS_NEXTCOSTUME,
    LOOKS_SAY,
    LOOKS_SAYFORSECS,
    LOOKS_SETEFFECTTO,
    LOOKS_SETSIZETO,
    LOOKS_SHOW,
    LOOKS_SWITCHBACKDROPTO,
    LOOKS_SWITCHBACKDROPTOANDWAIT,
    LOOKS_SWITCHCOSTUMETO,
    LOOKS_THINK,
    LOOKS_THINKFORSECS,

    MOTION_CHANGEXBY,
    MOTION_CHANGEYBY,
    MOTION_GLIDETO,
    MOTION_GLIDETOXY,
    MOTION_GOTO,
    MOTION_GOTOXY,
    MOTION_IFONEDGEBOUNCE,
    MOTION_MOVESTEPS,
    MOTION_POINTINDIRECTION,
    MOTION_POINTTOWARDS,
    MOTION_SETROTATIONSTYLE,
    MOTION_SETX,
    MOTION_SETY,
    MOTION_TURNLEFT,
    MOTION_TURNRIGHT,

    SENSING_ASKANDWAIT,
    SENSING_LOUD, // ToDo: ???
    SENSING_RESETTIMER,
    SENSING_SETDRAGMODE,

    SOUND_CHANGEEFFECTBY,
    SOUND_CHANGEVOLUMEBY,
    SOUND_CLEAREFFECTS,
    SOUND_PLAY,
    SOUND_PLAYUNTILDONE,
    SOUND_SETEFFECTTO,
    SOUND_SETVOLUMETO,
    SOUND_STOPALLSOUNDS,

    PEN_CHANGECOLORBY,
    PEN_CHANGEPENSIZEBY,
    PEN_CLEAR,
    PEN_PENDOWN,
    PEN_PENUP,
    PEN_SETCOLORTO,
    PEN_SETPENCOLORTOCOLOR,
    PEN_SETPENSIZETO,
    PEN_STAMP,

    TTS_SETLANGUAGE,
    TTS_SETVOICE,
    TTS_SPEAK,

    MUSIC_PLAYDRUMFORBEATS,
    MUSIC_RESTFORBEATS,
    MUSIC_PLAYNOTEFORBEATS,
    MUSIC_SETINSTRUMENTTO,
    MUSIC_SETTEMPOTO,
    MUSIC_CHANGETEMPOBY,

    // end-marker for a block, easier to add it as a statement rather than introducing special cases in other places
    END,
}
