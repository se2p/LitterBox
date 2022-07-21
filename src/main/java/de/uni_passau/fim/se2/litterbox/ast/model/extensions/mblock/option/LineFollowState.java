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

public class LineFollowState extends AbstractNode implements MBlockOption {

    private final LineFollowType type;

    public LineFollowState(String followName) {
        this.type = LineFollowType.fromString(followName);
    }

    public LineFollowType getLineFollowType() {
        return type;
    }

    public String getLineFollowDefinition() {
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
        if (!(o instanceof LineFollowState)) return false;
        LineFollowState that = (LineFollowState) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public enum LineFollowType {
        NONE("0"),
        RIGHT("1"),
        LEFT("2"),
        ALL("3");

        private final String definition;

        LineFollowType(String definition) {
            this.definition = Preconditions.checkNotNull(definition);
        }

        public static LineFollowType fromString(String name) {
            for (LineFollowType f : values()) {
                if (f.getDefinition().equals(name.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown Line Follow Type: " + name);
        }

        public String getDefinition() {
            return definition;
        }
    }
}
