package scratch.newast.parser.stmt;

import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.elementchoice.ElementChoice;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.statement.spritelook.ChangeSizeBy;
import scratch.newast.model.statement.spritelook.Hide;
import scratch.newast.model.statement.spritelook.Say;
import scratch.newast.model.statement.spritelook.SayForSecs;
import scratch.newast.model.statement.spritelook.SetSizeTo;
import scratch.newast.model.statement.spritelook.Show;
import scratch.newast.model.statement.spritelook.SpriteLookStmt;
import scratch.newast.model.statement.spritelook.SwitchCostumeTo;
import scratch.newast.model.statement.spritelook.Think;
import scratch.newast.model.statement.spritelook.ThinkForSecs;
import scratch.newast.opcodes.SpriteLookStmtOpcode;
import scratch.newast.parser.ElementChoiceParser;
import scratch.newast.parser.ExpressionParser;

public class SpriteLookStmtParser {

    public static SpriteLookStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(SpriteLookStmtOpcode.contains(opcodeString),
                "Given blockID does not point to a sprite look block. Opcode is " + opcodeString);

        SpriteLookStmtOpcode opcode = SpriteLookStmtOpcode.valueOf(opcodeString);
        StringExpr stringExpr;
        NumExpr numExpr;

        switch (opcode) {
            case looks_show:
                return new Show();
            case looks_hide:
                return new Hide();
            case looks_sayforsecs:
                stringExpr = ExpressionParser.parseStringExpr(current, 0, allBlocks);
                numExpr = ExpressionParser.parseNumExpr(current, 1, allBlocks);
                return new SayForSecs(stringExpr, numExpr);
            case looks_say:
                stringExpr = ExpressionParser.parseStringExpr(current, 0, allBlocks);
                return new Say(stringExpr);
            case looks_thinkforsecs:
                stringExpr = ExpressionParser.parseStringExpr(current, 0, allBlocks);
                numExpr = ExpressionParser.parseNumExpr(current, 1, allBlocks);
                return new ThinkForSecs(stringExpr, numExpr);
            case looks_think:
                stringExpr = ExpressionParser.parseStringExpr(current, 0, allBlocks);
                return new Think(stringExpr);
            case looks_switchcostumeto:
                ElementChoice choice = ElementChoiceParser.parse(current, allBlocks);
                return new SwitchCostumeTo(choice);
            case looks_changesizeby:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new ChangeSizeBy(numExpr);
            case looks_setsizeto:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new SetSizeTo(numExpr);
            case looks_gotofrontback:
            case looks_goforwardbackwardlayers:
            default:
                throw new RuntimeException("Not implemented for opcode " + opcodeString);
        }
    }
}
