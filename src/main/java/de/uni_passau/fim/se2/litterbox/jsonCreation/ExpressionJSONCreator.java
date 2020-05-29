package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.MOUSE;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.RANDOM;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.createBlockWithoutMutationString;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.createFields;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.StmtListJSONCreator.EMPTY_VALUE;

public class ExpressionJSONCreator implements ScratchVisitor {
    private List<String> finishedJSONStrings;
    private String previousBlockId = null;
    private String topExpressionId = null;

    public IdJsonStringTuple createExpressionJSON(String parentId, Expression expression) {
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
}
