package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class MCorePort extends AbstractNode implements MBlockOption {

    private final PortType type;

    public MCorePort(String portName) {
        this.type = PortType.fromString(portName);
    }

    public PortType getPortType() {
        return type;
    }

    public String getPortDefinition() {
        return type.getDefinition();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MBlockVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getUniqueName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = type.getDefinition();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MCorePort)) return false;
        MCorePort that = (MCorePort) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public enum PortType {
        PORT_1("1"),
        PORT_2("2"),
        PORT_3("3"),
        PORT_4("4"),
        PORT_ON_BOARD("6");

        private final String definition;

        PortType(String definition) {
            this.definition = Preconditions.checkNotNull(definition);
        }

        public static PortType fromString(String name) {
            for (PortType f : values()) {
                if (f.getDefinition().equals(name.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown Port Type: " + name);
        }

        public String getDefinition() {
            return definition;
        }
    }
}
