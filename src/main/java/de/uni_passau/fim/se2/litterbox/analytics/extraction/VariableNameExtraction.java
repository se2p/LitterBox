package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.analytics.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariableNameExtraction implements ScratchVisitor, NameExtraction {
    public static final String NAME = "variable_names";
    private static List<String> names;

    @Override
    public List<String> extractNames(Program program) {
        names = new ArrayList<>();
        Map<String, ExpressionListInfo> variables = program.getSymbolTable().getLists();
        return names;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
