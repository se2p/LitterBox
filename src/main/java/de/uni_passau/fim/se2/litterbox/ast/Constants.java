/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast;

public class Constants {

    /**
     * Constants used for serialization and deserialization for Scratch 3.
     * Names and values are the same as in the Scratch 3 source code.
     */
    public static final int INPUT_SAME_BLOCK_SHADOW = 1; // unobscured shadow
    public static final int INPUT_BLOCK_NO_SHADOW = 2; // no shadow
    public static final int INPUT_DIFF_BLOCK_SHADOW = 3; // obscured shadow
    public static final int MATH_NUM_PRIMITIVE = 4; // number
    public static final int POSITIVE_NUM_PRIMITIVE = 5; // positive number
    public static final int WHOLE_NUM_PRIMITIVE = 6; // positive integer
    public static final int INTEGER_NUM_PRIMITIVE = 7; // integer
    public static final int ANGLE_NUM_PRIMITIVE = 8; // angle
    public static final int COLOR_PICKER_PRIMITIVE = 9; // colour
    public static final int TEXT_PRIMITIVE = 10; // string
    public static final int BROADCAST_PRIMITIVE = 11; // broadcast
    public static final int VAR_PRIMITIVE = 12; // variable
    public static final int LIST_PRIMITIVE = 13; // list

    /**
     * The terms "input array" and "(input) data array" refer to specific parts
     * in the JSON file where inputs are stored.
     *
     * In the example
     * "DEGREES": [1,[4,"15"]]
     *
     * [1,[4,"15"]] is the input array
     * holding the input shadow indicator and the data array
     *
     * and [4,"15"] is the data array holding the input type and the input value.
     *
     * If the input array holds a variable or a list, there is another array
     * holding information about the obscured input. This array is called
     * "shadow array".
     *
     * In the example
     * "inputs": {"DEGREES": [3, [12, "meine Variable","`jEk@4|i[#Fk?(8x)AV.-my variable"], [4,"40"]]},
     *
     * [3, [12, "meine Variable","`jEk@4|i[#Fk?(8x)AV.-my variable"], [4,"40"]]
     * is the input array holding input shadow indicator, data array and shadow array,
     *
     * [12, "meine Variable","`jEk@4|i[#Fk?(8x)AV.-my variable"] is the input array
     *
     * and [4,"40"] is the shadow array.
     */

    /**
     * The position of the input shadow indicator in the input array.
     */
    public static final int POS_INPUT_SHADOW = 0;

    /**
     * The position of the data array holding input type and input value in the input array.
     */
    public static final int POS_DATA_ARRAY = 1;

    /**
     * The position of the shadow array in the input array.
     */
    public static final int POS_SHADOW_ARRAY = 2;

    /**
     * The position of the input type in the input and the shadow array.
     */
    public static final int POS_INPUT_TYPE = 0;

    /**
     * The position of the input value in the input data array and the shadow array.
     */
    public static final int POS_INPUT_VALUE = 1;

    /**
     * The position of the inputID in the data array. The inputID is either
     * a variable ID or a list ID.
     */
    public static final int POS_INPUT_ID = 2;

    /**
     * The position of the field value in the field data array.
     */
    public static final int FIELD_VALUE = 0;

    /**
     * The position of the block ID in an expr array.
     */
    public static final int POS_BLOCK_ID = 1;

    /**
     * The position of the unique identifier in a LIST.
     */
    public static final int LIST_IDENTIFIER_POS = 1;

    /**
     * The position of the name in a LIST.
     */
    public static final int LIST_NAME_POS = 0;

    /**
     * The position of the unique identifier in a DATA_INPUT.
     */
    public static final int DATA_INPUT_IDENTIFIER_POS = 2;

    /**
     * The position of the name in a DATA_INPUT.
     */
    public static final int DATA_INPUT_NAME_POS = 1;

    /**
     * The position of the x in a top DATA_INPUT.
     */
    public static final int DATA_INPUT_X_POS = 3;

    /**
     * The position of the y in a top DATA_INPUT.
     */
    public static final int DATA_INPUT_Y_POS = 4;

    /**
     * The position of the unique identifier in a VARIABLE.
     */
    public static final int VARIABLE_IDENTIFIER_POS = 1;

    /**
     * The position of the name in a VARIABLE.
     */
    public static final int VARIABLE_NAME_POS = 0;

    /**
     * JSon Field Names of JSon Blocks
     */
    public static final String OPCODE_KEY = "opcode";
    public static final String FIELDS_KEY = "fields";
    public static final String LIST_KEY = "LIST";
    public static final String VALUE_KEY = "VALUE";
    public static final String VARIABLE_KEY = "VARIABLE";
    public static final String NEXT_KEY = "next";
    public static final String INPUTS_KEY = "inputs";
    public static final String PARENT_KEY = "parent";
    public static final String NAME_KEY = "name";
    public static final String IS_STAGE_KEY = "isStage";

    public static final String OPERATOR_KEY = "OPERATOR";

    /**
     * The position of the variable's value in the declaration.
     */
    public static final int DECLARATION_VARIABLE_VALUE_POS = 1;

    /**
     * The position of the variable's name in the declaration.
     */
    public static final int DECLARATION_VARIABLE_NAME_POS = 0;

    /**
     * The position of the list's name in the declaration.
     */
    public static final int DECLARATION_LIST_NAME_POS = 0;

    /**
     * The position of the list's values in the declaration.
     */
    public static final int DECLARATION_LIST_VALUES_POS = 1;

    public static final String ARGUMENTNAMES_KEY = "argumentnames";
    public static final String ARGUMENTIDS_KEY = "argumentids";

    public static final String VOLUME_KEY = "volume";
    public static final String LAYERORDER_KEY = "layerOrder";
    public static final String TEMPO_KEY = "tempo";
    public static final String VIDTRANSPARENCY_KEY = "videoTransparency";
    public static final String VIDSTATE_KEY = "videoState";
    public static final String VISIBLE_KEY = "visible";
    public static final String SECS_KEY = "SECS";
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String SIZE_KEY = "size";
    public static final String DIRECTION_KEY = "direction";
    public static final String DRAG_KEY = "draggable";
    public static final String ROTATIONSTYLE_KEY = "rotationStyle";
    public static final String EFFECT_KEY = "EFFECT";
    public static final String DRAGMODE_KEY = "DRAG_MODE";
    public static final String STYLE_KEY = "STYLE";
    public static final String PEN_SIZE_KEY = "pen_size";
    public static final String COLOR_PARAM_BIG_KEY = "COLOR_PARAM";
    public static final String COLOR_PARAM_LITTLE_KEY = "colorParam";
    public final static String MUTATION_KEY = "mutation";
    public final static String PROCCODE_KEY = "proccode";
    public final static String OPERAND1_KEY = "OPERAND1";
    public final static String OPERAND2_KEY = "OPERAND2";
    public final static String OPERAND_KEY = "OPERAND";
    public final static String NUMBER_NAME_KEY = "NUMBER_NAME";
    public final static String OBJECT_KEY = "OBJECT";
    public final static String ITEM_KEY = "ITEM";
    public final static String STRING_KEY = "STRING";
    public final static String STRING1_KEY = "STRING1";
    public final static String STRING2_KEY = "STRING2";
    public final static String LETTER_KEY = "LETTER";
    public final static String MESSAGE_KEY = "MESSAGE";
    public final static String CHANGE_KEY = "CHANGE";
    public final static String SIZE_KEY_CAP = "SIZE";
    public final static String QUESTION_KEY = "QUESTION";
    public final static String INDEX_KEY = "INDEX";
    public final static String NUM_KEY = "NUM";
    public final static String COLOR_KEY = "COLOR";
    public final static String COLOR2_KEY = "COLOR2";
    public final static String CONDITION_KEY = "CONDITION";
    public final static String DURATION_KEY = "DURATION";
    public final static String NUM1_KEY = "NUM1";
    public final static String NUM2_KEY = "NUM2";
    public final static String FROM_KEY = "FROM";
    public final static String TO_KEY = "TO";
    public final static String TIMES_KEY = "TIMES";
    public final static String STEPS_KEY = "STEPS";
    public final static String DEGREES_KEY = "DEGREES";
    public final static String DIRECTION_KEY_CAP = "DIRECTION";
    public final static String DX_KEY = "DX";
    public final static String DY_KEY = "DY";
    public final static String VOLUME_KEY_CAPS = "VOLUME";
    public final static String HEIGHT_KEY = "height";
    public final static String WIDTH_KEY = "width";
    public final static String COMMENTS_KEY = "comments";
    public final static String CURRENT_COSTUME_KEY = "currentCostume";
    public final static String BROADCASTS_KEY = "broadcasts";
    public final static String LISTS_KEY = "lists";
    public final static String VARIABLES_KEY = "variables";
    public final static String COSTUMES_KEY = "costumes";
    public final static String SOUNDS_KEY = "sounds";
    public final static String META_KEY = "meta";
    public final static String EXTENSIONS_KEY = "extensions";
    public final static String MONITORS_KEY = "monitors";
    public final static String SEMVER_KEY = "semver";
    public final static String VM_KEY = "vm";
    public final static String AGENT_KEY = "agent";
    public final static String BLOCKS_KEY = "blocks";
    public final static String TOPLEVEL_KEY = "topLevel";
    public final static String TARGETS_KEY = "targets";
    public final static String TEXT_TO_SPEECH_KEY = "textToSpeechLanguage";
    public final static String COMMENT_KEY = "comment";
    public final static String SHADOW_KEY = "shadow";
    public final static String CHILDREN_KEY = "children";
    public final static String TAG_NAME_KEY = "tagName";
    public final static String ARGUMENT_DEFAULTS_KEY ="argumentdefaults";
    public final static String WARP_KEY = "warp";
}