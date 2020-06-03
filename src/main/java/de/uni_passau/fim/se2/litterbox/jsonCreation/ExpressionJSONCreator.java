package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.FieldsMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.JSONStringCreator.createField;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.StmtListJSONCreator.EMPTY_VALUE;


public class ExpressionJSONCreator implements ScratchVisitor {
    private List<String> finishedJSONStrings;
    private String previousBlockId = null;
    private String topExpressionId = null;
    private SymbolTable symbolTable;
    private ExpressionJSONCreator expressionJSONCreator;
    private FixedExpressionJSONCreator fixedExprCreator;

    public IdJsonStringTuple createExpressionJSON(String parentId, ASTNode expression, SymbolTable symbolTable) {
        finishedJSONStrings = new ArrayList<>();
        topExpressionId = null;
        previousBlockId = parentId;
        this.symbolTable = symbolTable;
        expressionJSONCreator = new ExpressionJSONCreator();
        fixedExprCreator = new FixedExpressionJSONCreator();
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


    private void createSimpleExpression(NonDataBlockMetadata metadata) {
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

    @Override
    public void visit(Costume node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getType().getType());
    }

    @Override
    public void visit(Backdrop node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getType().getType());
    }

    @Override
    public void visit(Current node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getTimeComp().getLabel());
    }

    private void createFieldsExpression(NonDataBlockMetadata metadata, String fieldValue) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), fieldValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(LengthOfVar node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        String fieldsString = getListDataFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    private String getListDataFields(NonDataBlockMetadata metadata, Identifier identifier) {
        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        Preconditions.checkArgument(identifier instanceof Qualified, "Identifier of list has to be in Qualified");
        Qualified qual = (Qualified) identifier;
        Preconditions.checkArgument(qual.getSecond() instanceof ScratchList, "Qualified has to hold Scratch List");
        ScratchList list = (ScratchList) qual.getSecond();
        String id = symbolTable.getListIdentifierFromActorAndName(qual.getFirst().getName(), list.getName().getName());
        return createFields(fieldsMeta.getFieldsName(), list.getName().getName(), id);
    }

    @Override
    public void visit(Add node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(Minus node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(Mult node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(Div node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(LessThan node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), OPERAND1_KEY,
                OPERAND2_KEY);
    }

    @Override
    public void visit(BiggerThan node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), OPERAND1_KEY,
                OPERAND2_KEY);
    }

    @Override
    public void visit(Equals node) {
        mathOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(), OPERAND1_KEY,
                OPERAND2_KEY);
    }

    @Override
    public void visit(Or node) {
        boolOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2());
    }

    @Override
    public void visit(And node) {
        boolOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2());
    }

    @Override
    public void visit(Not node) {
        NonDataBlockMetadata metadata =(NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        if (node.getOperand1() instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, OPERAND_KEY, false));
        } else {
            inputs.add(createExpr(metadata, node.getOperand1(), OPERAND_KEY, false));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }


    @Override
    public void visit(Touching node) {
        NonDataBlockMetadata metadata =(NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable touchable = node.getTouchable();
        if (touchable instanceof SpriteTouchable || touchable instanceof Edge || touchable instanceof MousePointer){
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), touchable);
            finishedJSONStrings.add(tuple.getJsonString());
           inputs.add(createReferenceInput(TOUCHINGOBJECTMENU, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        }else{
            AsTouchable asTouchable = (AsTouchable) touchable;
            inputs.add(createExpr(metadata, asTouchable.getOperand1(), TOUCHINGOBJECTMENU, true));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    private void boolOperations(NonDataBlockMetadata metadata, BoolExpr expr1, BoolExpr expr2) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        if (expr1 instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, OPERAND1_KEY, false));
        } else {
            inputs.add(createExpr(metadata, expr1, OPERAND1_KEY, false));
        }

        if (expr2 instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, OPERAND2_KEY, false));
        } else {
            inputs.add(createExpr(metadata, expr2, OPERAND2_KEY, false));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    private void mathOperations(NonDataBlockMetadata metadata, Expression expr1, Expression expr2, String inputName1,
                                String inputName2) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();

        inputs.add(createExpr(metadata, expr1, inputName1,true));
        inputs.add(createExpr(metadata, expr2, inputName2,true));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    private String createExpr(NonDataBlockMetadata metadata, Expression expr, String inputName, boolean withDefault) {
        IdJsonStringTuple tuple = expressionJSONCreator.createExpressionJSON(metadata.getBlockId(), expr
                , symbolTable);
        if (tuple.getId() == null) {
            StringBuilder jsonString = new StringBuilder();
            createField(jsonString, inputName).append(tuple.getJsonString());
            return jsonString.toString();
        } else {
            finishedJSONStrings.add(tuple.getJsonString());
            return createReferenceJSON(tuple.getId(), inputName, withDefault);
        }
    }
}
