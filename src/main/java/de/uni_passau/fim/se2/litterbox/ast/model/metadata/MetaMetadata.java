package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class MetaMetadata extends AbstractNode implements Metadata, ASTLeaf {
    private String semver;
    private String vm;
    private String agent;

    public MetaMetadata(String semver, String vm, String agent) {
        super();
        this.semver = semver;
        this.vm = vm;
        this.agent = agent;
    }

    public String getSemver() {
        return semver;
    }

    public String getVm() {
        return vm;
    }

    public String getAgent() {
        return agent;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
