package newanalytics.utils;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.Program;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.common.ChangeAttributeBy;
import scratch.ast.model.statement.common.SetAttributeTo;
import scratch.ast.model.statement.common.SetStmt;
import scratch.ast.model.statement.pen.*;
import scratch.ast.opcodes.CommonStmtOpcode;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

import static scratch.ast.Constants.PEN_SIZE_KEY;

public class ProgramUsingPen implements IssueFinder, ScratchVisitor {
    public static final String NAME = "using_pen";
    public static final String SHORT_NAME = "usingPen";
    private boolean found = false;
    private List<String> actorNames = new LinkedList<>();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        actorNames = new LinkedList<>();
        program.accept(this);
        int count = 0;
        if (found) {
            count = 1;
        }
        return new IssueReport(NAME, count, actorNames, "");
    }

    @Override
    public void visit(PenDownStmt node) {
        found=true;
    }

    @Override
    public void visit(PenUpStmt node) {
        found=true;
    }

    @Override
    public void visit(PenClearStmt node) {
        found=true;
    }

    @Override
    public void visit(SetAttributeTo node) {
        if(node.getStringExpr().equals(new StringLiteral(PEN_SIZE_KEY))){
            found=true;
        }else{
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        found=true;
    }

    @Override
    public void visit(PenStampStmt node) {
        found=true;
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        found=true;
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        found=true;
    }

    @Override
    public void visit(ChangeAttributeBy node) {
        if(node.getAttribute().equals(new StringLiteral(PEN_SIZE_KEY))){
            found=true;
        }else{
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }
    }
}
