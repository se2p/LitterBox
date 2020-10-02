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

    /*
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
     * The position of the field reference in the field data array.
     */
    public static final int FIELD_REFERENCE = 1;

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
     * JSon Field Names of JSon Blocks.
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
    public static final String MUTATION_KEY = "mutation";
    public static final String PROCCODE_KEY = "proccode";
    public static final String OPERAND1_KEY = "OPERAND1";
    public static final String OPERAND2_KEY = "OPERAND2";
    public static final String OPERAND_KEY = "OPERAND";
    public static final String NUMBER_NAME_KEY = "NUMBER_NAME";
    public static final String OBJECT_KEY = "OBJECT";
    public static final String ITEM_KEY = "ITEM";
    public static final String STRING_KEY = "STRING";
    public static final String STRING1_KEY = "STRING1";
    public static final String STRING2_KEY = "STRING2";
    public static final String LETTER_KEY = "LETTER";
    public static final String MESSAGE_KEY = "MESSAGE";
    public static final String CHANGE_KEY = "CHANGE";
    public static final String SIZE_KEY_CAP = "SIZE";
    public static final String QUESTION_KEY = "QUESTION";
    public static final String INDEX_KEY = "INDEX";
    public static final String NUM_KEY = "NUM";
    public static final String COLOR_KEY = "COLOR";
    public static final String COLOR2_KEY = "COLOR2";
    public static final String CONDITION_KEY = "CONDITION";
    public static final String DURATION_KEY = "DURATION";
    public static final String NUM1_KEY = "NUM1";
    public static final String NUM2_KEY = "NUM2";
    public static final String FROM_KEY = "FROM";
    public static final String TO_KEY = "TO";
    public static final String TOWARDS_KEY = "TOWARDS";
    public static final String DISTANCETOMENU_KEY = "DISTANCETOMENU";
    public static final String TIMES_KEY = "TIMES";
    public static final String STEPS_KEY = "STEPS";
    public static final String DEGREES_KEY = "DEGREES";
    public static final String DIRECTION_KEY_CAP = "DIRECTION";
    public static final String DX_KEY = "DX";
    public static final String DY_KEY = "DY";
    public static final String VOLUME_KEY_CAPS = "VOLUME";
    public static final String HEIGHT_KEY = "height";
    public static final String WIDTH_KEY = "width";
    public static final String COMMENTS_KEY = "comments";
    public static final String CURRENT_COSTUME_KEY = "currentCostume";
    public static final String BROADCASTS_KEY = "broadcasts";
    public static final String LISTS_KEY = "lists";
    public static final String VARIABLES_KEY = "variables";
    public static final String COSTUMES_KEY = "costumes";
    public static final String SOUNDS_KEY = "sounds";
    public static final String META_KEY = "meta";
    public static final String EXTENSIONS_KEY = "extensions";
    public static final String MONITORS_KEY = "monitors";
    public static final String SEMVER_KEY = "semver";
    public static final String VM_KEY = "vm";
    public static final String AGENT_KEY = "agent";
    public static final String BLOCKS_KEY = "blocks";
    public static final String TOPLEVEL_KEY = "topLevel";
    public static final String TARGETS_KEY = "targets";
    public static final String TEXT_TO_SPEECH_KEY = "textToSpeechLanguage";
    public static final String COMMENT_KEY = "comment";
    public static final String SHADOW_KEY = "shadow";
    public static final String CHILDREN_KEY = "children";
    public static final String TAG_NAME_KEY = "tagName";
    public static final String ARGUMENT_DEFAULTS_KEY = "argumentdefaults";
    public static final String WARP_KEY = "warp";
    public static final String ASSET_ID_KEY = "assetId";
    public static final String MD5EXT_KEY = "md5ext";
    public static final String DATA_FORMAT_KEY = "dataFormat";
    public static final String BITMAP_KEY = "bitmapResolution";
    public static final String ROTATIONX_KEY = "rotationCenterX";
    public static final String ROTATIONY_KEY = "rotationCenterY";
    public static final String RATE_KEY = "rate";
    public static final String SAMPLE_COUNT_KEY = "sampleCount";
    public static final String HAS_NEXT_KEY = "hasnext";
    public static final String ID_KEY = "id";
    public static final String MODE_KEY = "mode";
    public static final String PARAMS_KEY = "params";
    public static final String SPRITE_NAME_KEY = "spriteName";
    public static final String VALUE_LOWER_KEY = "value";
    public static final String SLIDER_MIN_KEY = "sliderMin";
    public static final String SLIDER_MAX_KEY = "sliderMax";
    public static final String MIN_KEY = "min";
    public static final String MAX_KEY = "max";
    public static final String IS_DISCRETE_KEY = "isDiscrete";
    public static final String BLOCK_ID_KEY = "blockId";
    public static final String MINIMIZED_KEY = "minimized";
    public static final String TEXT_KEY = "text";
    public static final String SUBSTACK_KEY = "SUBSTACK";
    public static final String SUBSTACK2_KEY = "SUBSTACK2";
    public static final String MOUSE = "_mouse_";
    public static final String RANDOM = "_random_";
    public static final String COSTUME_INPUT = "COSTUME";
    public static final String BACKDROP_INPUT = "BACKDROP";
    public static final String RANDOM_BACKDROP = "random backdrop";
    public static final String NEXT_BACKDROP = "next backdrop";
    public static final String PREVIOUS_BACKDROP = "previous backdrop";
    public static final String SOUND_MENU = "SOUND_MENU";
    public static final String CLONE_OPTION = "CLONE_OPTION";
    public static final String BROADCAST_INPUT_KEY = "BROADCAST_INPUT";
    public static final String TOUCHINGOBJECTMENU = "TOUCHINGOBJECTMENU";
    public static final String TOUCHING_EDGE = "_edge_";
    public static final String KEY_OPTION = "KEY_OPTION";
    public static final String CUSTOM_BLOCK_KEY = "custom_block";
    public static final String EMPTY_VALUE = "{}";
    public static final String BOOLEAN_DEFAULT = "false";
    public static final String STRING_NUMBER_DEFAULT = "";
    public static final String NUMBER_DEFAULT = "1";
}
