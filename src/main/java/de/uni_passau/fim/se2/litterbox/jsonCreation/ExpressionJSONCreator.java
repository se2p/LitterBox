package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.createBlockWithoutMutationString;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.createTypeInput;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.StmtListJSONCreator.EMPTY_VALUE;


public class ExpressionJSONCreator implements ScratchVisitor {
    private List<String> finishedJSONStrings;
    private String previousBlockId = null;
    private String topExpressionId = null;

    public IdJsonStringTuple createExpressionJSON(String parentId, ASTNode expression) {
        finishedJSONStrings = new ArrayList<>();
        topExpressionId = null;
        previousBlockId = parentId;
        expression.accept(this);
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; i < finishedJSONStrings.size() - 1; i++) {
            jsonString.append(finishedJSONStrings.get(i)).append(",");
        }
        if (finishedJSONStrings.size() > 0) {
            jsonString.append(finishedJSONStrings.get(finishedJSONStrings.size() - 1));
        }
        return new IdJsonStringTuple(topExpressionId, jsonString.toString());
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE, ""));
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, ""));
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE, ""));
    }

    @Override
    public void visit(AsString node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(AsNumber node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(AsBool node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(FromNumber node) {
        node.getValue().accept(this);
    }

    @Override
    public void visit(BoolLiteral node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE,
                String.valueOf(node.getValue())));
    }

    @Override
    public void visit(StringLiteral node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE,
                node.getText()));
    }

    @Override
    public void visit(NumberLiteral node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE,
                String.valueOf((float) node.getValue())));
    }

    @Override
    public void visit(ColorLiteral node) {
        StringBuilder colorString = new StringBuilder();
        colorString.append("#").append(String.format("0x%02x", node.getRed()).substring(2)).append(String.format("0x" +
                "%02X", node.getGreen()).substring(2)).append(String.format("0x%02X",
                node.getBlue()).substring(2));
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, COLOR_PICKER_PRIMITIVE,
                colorString.toString()));
    }


    private void createSimpleExpression(NonDataBlockMetadata metadata){
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null, previousBlockId, EMPTY_VALUE,
                EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Answer node) {
       createSimpleExpression((NonDataBlockMetadata) node.getMetadata());

    }

    @Override
    public void visit(MouseX node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }
    @Override
    public void visit(MouseY node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(PositionX node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(PositionY node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Direction node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Size node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Volume node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(IsMouseDown node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Loudness node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Timer node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(DaysSince2000 node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Username node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }
}
