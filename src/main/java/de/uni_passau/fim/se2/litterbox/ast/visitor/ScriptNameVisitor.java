package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;

public class ScriptNameVisitor implements ScratchVisitor{

    private String name;

    public String getName(ScriptEntity node) {
        visitChildren(node);
        if(name!= null)
            return name.replaceAll("[^a-zA-Z\\d\\s|]", "|").trim();
        return null;
    }
    @Override
    public void visit(NonDataBlockMetadata node) {
        this.name= node.getBlockId();
    }

    @Override
    public void visit(DataBlockMetadata node) {
        this.name= node.getBlockId();
    }


}
