package de.uni_passau.fim.se2.litterbox.analytics.hint;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.VariableAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class ComparingLiteralsHintFactory {
    public static final String DEFAULT_TRUE = "comparing_literals_default_true";
    public static final String DEFAULT_FALSE = "comparing_literals_default_false";
    public static final String DEFAULT_VARIABLE = "comparing_literals_default_variable";
    public static final String DEFAULT_VARIABLE_EXISTS = "comparing_literals_default_variable_exists";
    public static final String DEFAULT_WITHOUT_INFORMATION = "comparing_literals_default_without_information";
    public static final String DEFAULT_VARIABLE_WITHOUT_INFORMATION = "comparing_literals_default_variable_without_information";
    public static final String DEFAULT_VARIABLE_EXISTS_WITHOUT_INFORMATION = "comparing_literals_default_variable_exists_without_information";
    public static final String WAIT_TRUE = "comparing_literals_wait_true";
    public static final String WAIT_FALSE = "comparing_literals_wait_false";
    public static final String WAIT_VARIABLE = "comparing_literals_wait_variable";
    public static final String WAIT_VARIABLE_EXISTS = "comparing_literals_wait_variable_exists";
    public static final String HINT_TRUE_FALSE = "TRUEFALSE";
    public static final String ALWAYS_NEVER = "ALWAYSNEVER";
    public static final String THEN_ELSE = "THENELSE";
    public static final String ALWAYS = "always";
    public static final String NEVER = "never";

    public static Hint generateHint(boolean value, boolean wait, ASTNode parent, boolean variable, ASTNode currentNode, ActorDefinition currentActor, Program program, ProcedureDefinition currentProcedure, Script currentScript) {
        Hint hint;
        boolean variableExits = false;
        boolean top = checkIfTopCond(parent);
        boolean topNot = checkIfNotIsTop(parent);
        if (variable) {
            variableExits = checkForVariableAsLiteral(currentNode, program, currentActor, currentProcedure, currentScript);
        }

        if (top || topNot) {
            if (wait) {
                if (variableExits) {
                    hint = getWaitHint(value, variable, WAIT_VARIABLE_EXISTS, WAIT_TRUE, WAIT_FALSE);
                } else {
                    hint = getWaitHint(value, variable, WAIT_VARIABLE, WAIT_TRUE, WAIT_FALSE);
                }
            } else {
                if (variableExits) {
                    hint = getWaitHint(value, variable, DEFAULT_VARIABLE_EXISTS, DEFAULT_TRUE, DEFAULT_FALSE);
                } else {
                    hint = getWaitHint(value, variable, DEFAULT_VARIABLE, DEFAULT_TRUE, DEFAULT_FALSE);
                }
            }
            if (top) {
                if (value) {
                    hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(ALWAYS));
                } else {
                    hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(NEVER));
                }
            } else {
                if (!value) {
                    hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(ALWAYS));
                } else {
                    hint.setParameter(ALWAYS_NEVER, IssueTranslator.getInstance().getInfo(NEVER));
                }

            }
            setBlock(parent, hint, top);
        } else {
            if (variable && variableExits) {
                hint = new Hint(DEFAULT_VARIABLE_EXISTS_WITHOUT_INFORMATION);
            } else if (variable) {
                hint = new Hint(DEFAULT_VARIABLE_WITHOUT_INFORMATION);
            } else {
                hint = new Hint(DEFAULT_WITHOUT_INFORMATION);
            }
            hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo(String.valueOf(value)));
        }
        return hint;
    }

    private static void setBlock(ASTNode parent, Hint hint, boolean isTop) {
        ASTNode currentParent;
        //If the parent is not at top it is a not node and one has to get one higher
        if (!isTop) {
            currentParent = parent.getParentNode();
        } else {
            currentParent = parent;
        }
        if (currentParent instanceof IfThenStmt || currentParent instanceof IfElseStmt) {
            hint.setParameter(THEN_ELSE, IssueTranslator.getInstance().getInfo("then"));
        } else if (currentParent instanceof UntilStmt) {
            hint.setParameter(THEN_ELSE, IssueTranslator.getInstance().getInfo("until"));
        }
    }

    private static Hint getWaitHint(boolean value, boolean variable, String waitVariable, String waitTrue, String waitFalse) {
        Hint hint;
        if (variable) {
            hint = new Hint(waitVariable);
            hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo(String.valueOf(value)));
        } else {
            if (value) {
                hint = new Hint(waitTrue);
            } else {
                hint = new Hint(waitFalse);
            }
        }
        return hint;
    }

    private static boolean checkForVariableAsLiteral(ASTNode currentNode, Program program, ActorDefinition currentActor, ProcedureDefinition currentProcedure, Script currentScript) {
        VariableAsLiteral finder = new VariableAsLiteral();
        Set<Issue> variablesAsLiterals = finder.check(program);
        for (Issue issue : variablesAsLiterals) {
            if (issue.getActor() == currentActor && issue.getCodeLocation() == currentNode && issue.getProcedure() == currentProcedure && issue.getScript() == currentScript) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfNotIsTop(ASTNode parent) {
        return parent instanceof Not && checkIfTopCond(parent.getParentNode());
    }

    private static boolean checkIfTopCond(ASTNode parent) {
        return parent instanceof IfThenStmt || parent instanceof IfElseStmt || parent instanceof UntilStmt || parent instanceof WaitUntil;
    }

    public static Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(DEFAULT_FALSE);
        keys.add(DEFAULT_TRUE);
        keys.add(DEFAULT_VARIABLE);
        keys.add(DEFAULT_VARIABLE_EXISTS);
        keys.add(WAIT_FALSE);
        keys.add(WAIT_TRUE);
        keys.add(WAIT_VARIABLE);
        keys.add(WAIT_VARIABLE_EXISTS);
        keys.add(DEFAULT_VARIABLE_EXISTS_WITHOUT_INFORMATION);
        keys.add(DEFAULT_VARIABLE_WITHOUT_INFORMATION);
        keys.add(DEFAULT_WITHOUT_INFORMATION);
        return keys;
    }
}
