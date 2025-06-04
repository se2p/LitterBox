package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Nothing
 * @Context Whole program
 */
public class ScriptToSetVariable extends AbstractQuestionFinder {
    private String variable;
    private Map<String, String> answers;

    @Override
    public void visit(Program node) {
        answers = new HashMap<>();
        super.visit(node);

        currentScript = null;
        currentProcedure = null;

        if (!choices.isEmpty() && !answers.isEmpty()) {
            answers.forEach((variable, answer) -> {
                IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.HINT_VARIABLE, variable);
                hint.setParameter(Hint.ANSWER, answer);
                hint.setParameter(Hint.CHOICES, getChoices());
                addIssue(builder.withHint(hint));
            });
        }
    }

    @Override
    public void visit(Script node) {
        variable = null;
        super.visit(node);
        if (!isNull(variable)) {
            answers.putIfAbsent(variable, wrappedScratchBlocks(node));
        }
        else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public void visit(SetVariableTo node) {
        variable = ScratchBlocksVisitor.of(node.getIdentifier());
    }

    @Override
    public String getName() {
        return "script_to_set_variable";
    }
}
