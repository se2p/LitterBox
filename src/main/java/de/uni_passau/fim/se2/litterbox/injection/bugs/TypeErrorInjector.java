package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TypeError;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the {@link TypeError} bug using two strategies:
 * <p>
 * <b>Strategy 1: Comparison Type Error</b><br>
 * Replaces one operand in comparisons (Equals, LessThan, BiggerThan) with a boolean.
 * <p>
 * <b>Strategy 2: Number-to-Boolean Injection</b><br>
 * Replaces NUMBER parameters (in MoveSteps, SetXTo, WaitSeconds, etc.) with a boolean expression.
 * <p>
 * For realistic bugs, this injector collects boolean expressions from the program
 * (like {@code Touching}, {@code IsMouseDown}, etc.) and uses them as replacement values.
 * <p>
 * This injector uses immutable transformations via {@link #addImmutableMutation} to
 * maintain AST immutability.
 */
public class TypeErrorInjector extends AbstractBugInjector {

    private static final NumExpr FALLBACK_BOOL_AS_NUM = new AsNumber(new BoolLiteral(true));

    private final List<BoolExpr> collectedBoolExprs = new ArrayList<>();
    private NumExpr boolAsNumReplacement;

    @Override
    public List<Program> inject(Program program) {
        // First pass: collect boolean expressions from the program
        collectExpressions(program);

        // Choose replacement expression
        if (!collectedBoolExprs.isEmpty()) {
            BoolExpr chosen = collectedBoolExprs.getFirst();
            BoolExpr cloned = (BoolExpr) chosen.accept(new CloneVisitor());
            boolAsNumReplacement = new AsNumber(cloned);
        } else {
            boolAsNumReplacement = FALLBACK_BOOL_AS_NUM;
        }

        // Second pass: inject bugs
        return super.inject(program);
    }

    private void collectExpressions(Program program) {
        collectedBoolExprs.clear();
        program.accept(new ExpressionCollector());
    }

    /**
     * Visitor that collects boolean expressions for realistic injection.
     */
    private class ExpressionCollector implements ScratchVisitor {
        @Override
        public void visit(Touching node) {
            collectedBoolExprs.add(node);
        }

        @Override
        public void visit(IsMouseDown node) {
            collectedBoolExprs.add(node);
        }

        @Override
        public void visit(IsKeyPressed node) {
            collectedBoolExprs.add(node);
        }

        @Override
        public void visit(SpriteTouchingColor node) {
            collectedBoolExprs.add(node);
        }
    }

    @Override
    public IssueFinder getIssueFinder() {
        return new TypeError();
    }

    // --- Strategy 1: Comparison Type Errors ---
    // Uses immutable transformations to replace operands

    @Override
    public void visit(Equals node) {
        if (node.getOperand1() instanceof NumberLiteral || node.getOperand1() instanceof StringLiteral) {
            addImmutableMutation(node, n -> new Equals(
                    n.getOperand1(),
                    getBoolAsNumReplacement(),
                    n.getMetadata()
            ));
        }
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        if (node.getOperand1() instanceof NumberLiteral || node.getOperand1() instanceof StringLiteral) {
            addImmutableMutation(node, n -> new LessThan(
                    n.getOperand1(),
                    getBoolAsNumReplacement(),
                    n.getMetadata()
            ));
        }
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if (node.getOperand1() instanceof NumberLiteral || node.getOperand1() instanceof StringLiteral) {
            addImmutableMutation(node, n -> new BiggerThan(
                    n.getOperand1(),
                    getBoolAsNumReplacement(),
                    n.getMetadata()
            ));
        }
        super.visit(node);
    }

    // --- Strategy 2: Number-to-Boolean Injection ---
    // Uses immutable transformations to replace numeric parameters

    @Override
    public void visit(MoveSteps node) {
        addImmutableMutation(node, n -> new MoveSteps(
                getBoolAsNumReplacement(),
                n.getMetadata()
        ));
        super.visit(node);
    }

    @Override
    public void visit(SetXTo node) {
        addImmutableMutation(node, n -> new SetXTo(
                getBoolAsNumReplacement(),
                n.getMetadata()
        ));
        super.visit(node);
    }

    @Override
    public void visit(SetYTo node) {
        addImmutableMutation(node, n -> new SetYTo(
                getBoolAsNumReplacement(),
                n.getMetadata()
        ));
        super.visit(node);
    }

    @Override
    public void visit(WaitSeconds node) {
        addImmutableMutation(node, n -> new WaitSeconds(
                getBoolAsNumReplacement(),
                n.getMetadata()
        ));
        super.visit(node);
    }

    private NumExpr getBoolAsNumReplacement() {
        return (NumExpr) boolAsNumReplacement.accept(new CloneVisitor());
    }
}
