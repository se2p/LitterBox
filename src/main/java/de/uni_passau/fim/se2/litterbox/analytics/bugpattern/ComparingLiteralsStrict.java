package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

/**
 * Reporter blocks are used to evaluate the truth value of certain expressions.
 * Not only is it possible to compare literals to variables or the results of other reporter blocks, literals can
 * also be compared to literals.
 * Since this will lead to the same result in each execution this construct is unnecessary and can obscure the fact
 * that certain blocks will never or always be executed.
 * This strict version only counts comparing literals when it is not in dead code.
 */
public class ComparingLiteralsStrict extends ComparingLiterals {
    public static final String NAME = "comparing_literals_strict";
    private static boolean inScriptOrProcedure;

    @Override
    public void visit(Script script) {
        currentScript = script;
        if (!(script.getEvent() instanceof Never)) {
            inScriptOrProcedure = true;
        }
        currentProcedure = null;
        visitChildren(script);
        inScriptOrProcedure = false;
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        currentProcedure = procedure;
        inScriptOrProcedure = true;
        currentScript = null;
        visitChildren(procedure);
        inScriptOrProcedure = false;
    }

    @Override
    public void visit(Equals node) {
        if (inScriptOrProcedure) {
            if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                    && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
                addIssue(node, node.getMetadata());
            }
            visitChildren(node);
        }
    }

    @Override
    public void visit(LessThan node) {
        if (inScriptOrProcedure) {
            if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                    && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
                addIssue(node, node.getMetadata());
            }
            visitChildren(node);
        }
    }

    @Override
    public void visit(BiggerThan node) {
        if (inScriptOrProcedure) {
            if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                    && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
                addIssue(node, node.getMetadata());
            }
            visitChildren(node);
        }
    }
}
