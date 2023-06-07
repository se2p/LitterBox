package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;

/**
 * generating a unique id to ScriptEntity across multiple runs by returning the first blockID of its statements
 * a blockID should be unique within a scratch programs,
 *
 * In rare cases where a JSON file might have been edited, it might not!
 * 444907338_17-06-2021_19-23-06.Json project is an example for such cases??
 */
public class ScriptEntityNameVisitor implements ScratchVisitor{

    private String name;
    private boolean foundFirstBlockId;

    public String getName(ScriptEntity node) {
        visitChildren(node);
        if(name!= null){
            if (node instanceof Script) {
                return "ScriptId_" + name.hashCode();
            }
            else return "ProcedureId_" + name.hashCode();
        }
        return null;
    }
    @Override
    public void visit(NonDataBlockMetadata node) {
        if (!foundFirstBlockId) {
            this.name = node.getBlockId();
            foundFirstBlockId = true;
        }
    }

    @Override
    public void visit(DataBlockMetadata node) {
        if (!foundFirstBlockId) {
            this.name = node.getBlockId();
            foundFirstBlockId = true;
        }
    }


}
