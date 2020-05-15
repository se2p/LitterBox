package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class PrototypeMutationMetadata extends CallMutationMetadata {
    private String argumentNames;
    private String argumentDefaults;


    public PrototypeMutationMetadata(String tagName, List<String> children, String procCode, String argumentIds,
                                     boolean warp, String argumentNames, String argumentDefaults) {
        super(tagName, children, procCode, argumentIds, warp);
        this.argumentNames = argumentNames;
        this.argumentDefaults = argumentDefaults;
    }

    public String getArgumentNames() {
        return argumentNames;
    }

    public String getArgumentDefaults() {
        return argumentDefaults;
    }


    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
