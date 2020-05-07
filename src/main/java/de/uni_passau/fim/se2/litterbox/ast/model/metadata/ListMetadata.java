package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ListMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private String listId;
    private String listName;
    private List<String> values;

    public ListMetadata(String variableID, String listName, List<String> values) {
        super();
        this.listId = variableID;
        this.listName = listName;
        this.values = values;
    }

    public String getListId() {
        return listId;
    }

    public String getListName() {
        return listName;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
