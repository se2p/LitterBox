package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Round;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

public class HalsteadVisitor implements ScratchVisitor {

    private Multiset<Class<?>> operators = LinkedHashMultiset.create();

    private Multiset<ASTNode> operands  = LinkedHashMultiset.create();

    public int getTotalOperands() {
        return operands.size();
    }

    public int getTotalOperators() {
        return operators.size();
    }

    public int getUniqueOperands() {
        return operands.elementSet().size();
    }

    public int getUniqueOperators() {
        return operators.elementSet().size();
    }

    @Override
    public void visit(Program program) {
        for(ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            visit(actor.getScripts());
            visit(actor.getProcedureDefinitionList());
        }
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        operators.add(procedure.getClass());
        visit(procedure.getParameterDefinitionList());
        visit(procedure.getStmtList());
    }

    //---------------------------------------------------------------
    // Operands
    //---------------------------------------------------------------

    @Override
    public void visit(BoolLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(ColorLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(NumberLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(StringLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(Identifier node) {
        operands.add(node);
    }

    @Override
    public void visit(Position node) {
        operands.add(node);
    }

    @Override
    public void visit(ElementChoice node) {
        operands.add(node);
    }

    @Override
    public void visit(Key node) {
        operands.add(node);
    }

    @Override
    public void visit(NumFunct node) {
        // Debatable?
        operands.add(node);
    }

    @Override
    public void visit(EventAttribute node) {
        operands.add(node);
    }

    @Override
    public void visit(FixedAttribute node) {
        operands.add(node);
    }

    @Override
    public void visit(TimeComp node) {
        operands.add(node);
    }

    @Override
    public void visit(NameNum node) {
        operands.add(node);
    }

    @Override
    public void visit(Edge node) {
        operands.add(node);
    }

    @Override
    public void visit(MousePointer node) {
        operands.add(node);
    }

    @Override
    public void visit(SpriteTouchable node) {
        operands.add(node);
    }

    // TODO: UnspecifiedBoolExpression (i.e. empty slot for comparison) are currently counted as operators

    //---------------------------------------------------------------
    // Operators
    //---------------------------------------------------------------

    @Override
    public void visit(Expression node) {
        // TODO: Refactor once this works
        if (node instanceof SingularExpression) {
            if (node instanceof IsMouseDown) {
                operators.add(node.getClass());
            } else {
                // Answer, Username, Direction, Loudness, ...
                operands.add(node);
            }
        } else if (node instanceof Parameter) {
            visitChildren(node);
        } else if (node instanceof UnaryExpression) {
            if (node instanceof Round || node instanceof Not) {
                // Skips AsNumber, AsBool, etc.
                operators.add(node.getClass());
            }
            visitChildren(node);
        } else if (node instanceof BinaryExpression || node instanceof BoolExpr || node instanceof ComparableExpr) {
            operators.add(node.getClass());
            visitChildren(node);
        }
    }

    @Override
    public void visit(Stmt node) {
        operators.add(node.getClass());
        visitChildren(node);
    }

    @Override
    public void visit(CallStmt node) {
        operators.add(node.getClass());
        visit(node.getExpressions());
    }

    @Override
    public void visit(Event node) {
        if (!(node instanceof Never)) {
            operators.add(node.getClass());
        }
        visitChildren(node);
    }
}
