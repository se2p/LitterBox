package scratch.newast.model.procedure;

import java.util.List;

public class ProcedureDeclarationList {

    private List<ProcedureDeclaration> list;

    public ProcedureDeclarationList(List<ProcedureDeclaration> list) {
        this.list=list;
    }

    public List<ProcedureDeclaration> getList() {
        return list;
    }

    public void setList(List<ProcedureDeclaration> list) {
        this.list = list;
    }
}
