package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Detects type errors of the following form:
 *  - Comparisons between (empty) String and Number
 *  - Comparisons between Direction and Loudness
 *  - Comparisons between Direction and Position nodes (MouseX, MouseY, PositionX, PositionY)
 *  - Comparisons between Loudness and Position nodes (MouseX, MouseY, PositionX, PositionY)
 *  - Comparisons containing Touching or Not nodes
 */
public class TypeError implements IssueFinder, ScratchVisitor {
    public static final String NAME = "type_error";
    public static final String SHORT_NAME = "typeError";
    private static final String NOTE1 = "There are no type errors in your project.";
    private static final String NOTE2 = "Some of the comparisons compare attributes of different types.";
    private int count = 0;
    private boolean found;
    private List<String> actorNames = new LinkedList<>();
    private boolean insideComparison = false;
    private boolean isRightSide = false;
    private String type = null;

    @Override
    public IssueReport check(Program program){
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        program.accept(this);
        String notes = NOTE1;
        if (found) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Needed when dead code should be excluded.
     *
     * @param node LessThan Node of which the children will be iterated
     */
    /*@Override
    public void visit(Script node) {
        if(!node.getEvent().getUniqueName().equals("Never")) {
            visit((ASTNode) node);
        }
    }*/

    @Override
    public void visit(LessThan node) {
        comparison(node);
    }

    @Override
    public void visit(BiggerThan node) {
        comparison(node);
    }

    @Override
    public void visit(Equals node) {
        comparison(node);
    }

    private void comparison(BinaryExpression<ComparableExpr, ComparableExpr> node){
        insideComparison = true;
        if (!node.getChildren().isEmpty()) {
            isRightSide = false;
            ASTNode leftChild = node.getChildren().get(0);
            leftChild.accept(this);
            ASTNode rightChild = node.getChildren().get(1);
            isRightSide = true;
            rightChild.accept(this);
        }
        insideComparison = false;
        type = null;
    }

    @Override
    public void visit(StringExpr node) {
        if(insideComparison) {
            if (!isRightSide) {
                type = null;
            }
        }
    }

    @Override
    public void visit(Loudness node) {
        checker("Loudness");
    }

    @Override
    public void visit(StringLiteral node) {
        if(insideComparison){
            if(!isRightSide){
                this.type = "String";
            } else {
                if(this.type != null && !type.equals("String")){
                    count++;
                    found = true;
                }
            }
        }
    }

    @Override
    public void visit(MouseX node) {
        checker("Position");
    }

    @Override
    public void visit(MouseY node) {
        checker("Position");
    }

    @Override
    public void visit(PositionX node) {
        checker("Position");
    }

    @Override
    public void visit(PositionY node) {
        checker("Position");
    }

    /*@Override
    public void visit(PickRandom node) {
        if(insideComparison) {
            checker("Number");
        }
    }*/

   @Override
    public void visit(NumExpr node) {
       if (insideComparison) {
           if (!isRightSide) {
               type = "Number";
           } else {
               if (this.type != null && (this.type.equals("String") || this.type.equals("Boolean"))) {
                   count++;
                   found = true;
               }
           }
       }
   }


    @Override
    public void visit(Direction node) {
        checker("Direction");
    }


    @Override
    public void visit(AsString node) {
        if(insideComparison){
            if(!isRightSide){
                if(node.getOperand1().getUniqueName().equals("Touching")){
                    type = "Boolean";
                } else if (node.getOperand1().getUniqueName().equals("Not")) {
                    type = "Boolean";
                } else {
                    type = null;
                }
            } else {
                if(node.getOperand1().getUniqueName().equals("Touching") || node.getOperand1().getUniqueName().equals("Not")) {
                    count++;
                    found = true;
                }
            }
        }
    }

    @Override
    public void visit(AsNumber node) {
        if(insideComparison) {
            if (!isRightSide) {
                type = null;
            } else {
                if(this.type.equals("Boolean")){
                    count++;
                    found = true;
                }
            }
        }
    }


    @Override
    public void visit(NumberLiteral node) {
        if(insideComparison){
            if(!isRightSide){
                type = "Number";
            } else {
                if(this.type != null && (this.type.equals("String") || this.type.equals("Boolean"))){
                    count++;
                    found = true;
                }
            }
        }
    }

    private void checker(String type){
        if(insideComparison){
            if(!isRightSide){
                this.type = type;
            } else {
                if(this.type != null && !this.type.equals(type) && !this.type.equals("Number")){
                    count++;
                    found = true;
                }
            }
        }
    }
}