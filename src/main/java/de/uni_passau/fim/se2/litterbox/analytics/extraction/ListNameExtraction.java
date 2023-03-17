package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.analytics.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListNameExtraction implements ScratchVisitor, NameExtraction {
    public static final String NAME = "list_names";
    private static List<String> names;

    @Override
    public List<String> extractNames(Program program) {
        names = new ArrayList<>();
        Collection<ExpressionListInfo> variables = program.getSymbolTable().getLists().values();
        for (ExpressionListInfo variable : variables) {
            names.add(variable.getVariableName());
        }
        return names;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
