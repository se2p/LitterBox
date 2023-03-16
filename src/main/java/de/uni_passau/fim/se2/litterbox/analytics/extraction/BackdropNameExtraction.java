package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.analytics.NameExtraction;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.ArrayList;
import java.util.List;

public class BackdropNameExtraction implements ScratchVisitor, NameExtraction {
    public static final String NAME = "backdrop_names";
    private static List<String> names;
    private boolean inActor;

    @Override
    public List<String> extractNames(Program program) {
        names = new ArrayList<>();
        program.accept(this);
        return names;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (node.isStage()) {
            inActor = true;
        }
        visitChildren(node);
        inActor = false;
    }

    @Override
    public void visit(ImageMetadata node) {
        if (inActor) {
            names.add(node.getName());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
