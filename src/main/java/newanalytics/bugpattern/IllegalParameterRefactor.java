package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.Constants;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.expression.bool.And;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.bool.Not;
import scratch.ast.model.expression.bool.Or;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.statement.common.WaitUntil;
import scratch.ast.model.statement.control.IfElseStmt;
import scratch.ast.model.statement.control.IfThenStmt;
import scratch.ast.model.statement.control.UntilStmt;
import scratch.ast.model.type.BooleanType;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.symboltable.ArgumentInfo;
import scratch.ast.parser.symboltable.ProcedureInfo;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IllegalParameterRefactor implements IssueFinder, ScratchVisitor {
    public static final String NAME = "illegal_parameter_refactor";
    public static final String SHORT_NAME = "illParamRefac";
    private static final String NOTE1 = "There are no procedures with illegally refactored parameters in your project.";
    private static final String NOTE2 = "Some of the sprites contain procedures with illegally refactored parameters.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private Map<Identifier, ProcedureInfo> procedureMap;
    private ArgumentInfo[] currentArguments;
    private boolean insideProcedure;
    private Program program;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procedureMap=program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentArguments = procedureMap.get(node.getIdent()).getArguments();
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    private void checkBool(BoolExpr boolExpr) {
        if (boolExpr instanceof Identifier) {
            Identifier ident = (Identifier) boolExpr;
            if (ident.getName().startsWith(Constants.PARAMETER_ABBREVIATION)) {
                for (ArgumentInfo currentArgument : currentArguments) {
                    if (currentArgument.getName().equals(ident.getName()) && !(currentArgument.getType() instanceof BooleanType)) {
                        found = true;
                        count++;
                    }
                }
            }
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (insideProcedure) {
            checkBool(node.getUntil());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Not node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(And node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Or node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
