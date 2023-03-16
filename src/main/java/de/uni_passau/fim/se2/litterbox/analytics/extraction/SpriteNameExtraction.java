package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.analytics.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

public class SpriteNameExtraction implements ScratchVisitor, NameExtraction {
    public static final String NAME = "sprite_names";
    private static List<String> names;

    @Override
    public List<String> extractNames(Program program) {
        names = new ArrayList<>();
        program.accept(this);
        return names;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (node.isSprite()) {
            names.add(node.getIdent().getName());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
