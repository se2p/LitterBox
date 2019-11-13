package scratch.newast;

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
     * JSon Field Names of JSon Blocks
     */
    public static final String OPCODE_KEY = "opcode";
    public static final String FIELDS_KEY = "fields";
    public static final String NEXT_KEY = "next";
    public static final String INPUTS_KEY = "inputs";
    public static final String PARENT_KEY = "parent";

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
}