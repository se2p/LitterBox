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

public class RobotButton extends AbstractNode implements MBlockOption {

    private final EventButtonType buttonType;

    public RobotButton(String buttonName) {
        this.buttonType = EventButtonType.fromString(buttonName);
    }

    public EventButtonType getButtonType() {
        return buttonType;
    }

    public String getButtonName() {
        return buttonType.getName();
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
        result[0] = buttonType.getName();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RobotButton)) return false;
        RobotButton that = (RobotButton) o;
        return buttonType == that.buttonType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buttonType);
    }

    public enum EventButtonType {
        A("a"),
        B("b"),
        C("c");
        private final String name;

        EventButtonType(String name) {
            this.name = Preconditions.checkNotNull(name);
        }

        public static EventButtonType fromString(String name) {
            for (EventButtonType f : values()) {
                if (f.getName().equals(name.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown RobotButton: " + name);
        }

        public String getName() {
            return name;
        }
    }
}
