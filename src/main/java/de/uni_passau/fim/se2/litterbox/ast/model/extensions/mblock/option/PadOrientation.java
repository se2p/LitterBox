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

public class PadOrientation extends AbstractNode implements MBlockOption {

    private final OrientationType type;

    public PadOrientation(String orientationName) {
        this.type = OrientationType.fromString(orientationName);
    }

    public OrientationType getOrientationType() {
        return type;
    }

    public String getOrientationName() {
        return type.getName();
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
        result[0] = type.getName();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PadOrientation)) return false;
        PadOrientation that = (PadOrientation) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public enum OrientationType {
        SCREEN_UP("screen_up"),
        SCREEN_DOWN("screen_down"),
        UPRIGHT("upright");
        private final String name;

        OrientationType(String name) {
            this.name = Preconditions.checkNotNull(name);
        }

        public static OrientationType fromString(String name) {
            for (OrientationType f : values()) {
                if (f.getName().equals(name.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown Direction Type: " + name);
        }

        public String getName() {
            return name;
        }
    }
}
