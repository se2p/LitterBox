package scratch.newast.model;

import java.util.List;

public class DeclarationList {

    List<Declaration> declarationList;

    public DeclarationList(List<Declaration> declarationList) {
        this.declarationList = declarationList;
    }

    public List<Declaration> getDeclarationList() {
        return declarationList;
    }

    public void setDeclarationList(List<Declaration> declarationList) {
        this.declarationList = declarationList;
    }
}
