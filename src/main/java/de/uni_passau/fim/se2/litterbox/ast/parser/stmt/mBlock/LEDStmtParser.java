package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mBlock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDColor;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RGB;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.LEDStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class LEDStmtParser {
    private final static String COLORLIST_KEY = "COLORLIST";
    private static final String LED_POSITION_KEY = "LED_POSTION";   // spelling error in mBlock
    private static final String RED_KEY = "R";
    private static final String GREEN_KEY = "G";
    private static final String BLUE_KEY = "B";

    public static LEDStmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(LEDStmtOpcode.contains(opcodeString), "Given blockId does not point to an LED block.");

        LEDStmtOpcode opcode = LEDStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case show_led_with_time:
                StringExpr colorString = StringExprParser.parseStringExpr(state, current, COLOR_KEY, blocks);
                NumExpr time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new LEDColorTimed(colorString, time, metadata);

            case show_led:
                if (opcodeString.contains("mcore.")) {
                    String positionName = current.get(FIELDS_KEY).get(LED_POSITION_KEY).get(0).asText();
                    LEDPosition position = new LEDPosition(positionName);
                    colorString = StringExprParser.parseStringExpr(state, current, COLOR_KEY, blocks);
                    return new LEDColorShowPosition(position, colorString, metadata);
                }
                colorString = StringExprParser.parseStringExpr(state, current, COLOR_KEY, blocks);
                return new LEDColorShow(colorString, metadata);

            case show_led_rgb:
                if (opcodeString.contains("mcore.")) {
                    String positionName = current.get(FIELDS_KEY).get(LED_POSITION_KEY).get(0).asText();
                    LEDPosition position = new LEDPosition(positionName);
                    NumExpr red = NumExprParser.parseNumExpr(state, current, RED_KEY, blocks);
                    NumExpr green = NumExprParser.parseNumExpr(state, current, GREEN_KEY, blocks);
                    NumExpr blue = NumExprParser.parseNumExpr(state, current, BLUE_KEY, blocks);
                    return new RGBValuesPosition(position, red, green, blue, metadata);
                }
                String rgbName = current.get(FIELDS_KEY).get(RGB_KEY).get(0).asText();
                RGB rgb = new RGB(rgbName);
                NumExpr value = NumExprParser.parseNumExpr(state, current, VALUE_KEY, blocks);
                return new RGBValue(rgb, value, metadata);

            case turn_off_led:
                return new LEDOff(metadata);

            case rocky_show_led_color:
                String colorName = current.get(FIELDS_KEY).get(COLORLIST_KEY).get(0).asText();
                LEDColor LEDColor = new LEDColor(colorName);
                return new RockyLight(LEDColor, metadata);

            case rocky_turn_off_led_color:
                return new RockyLightOff(metadata);

            case show_led_time:
                String positionName = current.get(FIELDS_KEY).get(LED_POSITION_KEY).get(0).asText();
                LEDPosition position = new LEDPosition(positionName);
                colorString = StringExprParser.parseStringExpr(state, current, COLOR_KEY, blocks);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new LEDColorTimedPosition(position, colorString, time, metadata);

            default:
                throw new IllegalStateException("LEDStmt Block with opcode " + opcode + " was not parsed");
        }
    }
}

