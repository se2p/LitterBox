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

public class LEDPosition extends AbstractNode implements MBlockOption {

    private final PositionType type;

    public LEDPosition(String rgbName) {
        this.type = PositionType.fromString(rgbName);
    }

    public PositionType getPositionType() {
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
        if (!(o instanceof LEDPosition)) return false;
        LEDPosition that = (LEDPosition) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public enum PositionType {
        ALL("0"),
        RIGHT("1"),
        LEFT("2"),
        ;

        private final String definition;

        PositionType(String definition) {
            this.definition = Preconditions.checkNotNull(definition);
        }

        public static PositionType fromString(String name) {
            for (PositionType f : values()) {
                if (f.getDefinition().equals(name.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown Position Type: " + name);
        }

        public String getDefinition() {
            return definition;
        }
    }
}
