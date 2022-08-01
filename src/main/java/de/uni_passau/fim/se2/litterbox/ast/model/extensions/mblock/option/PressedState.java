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

public class PressedState extends AbstractNode implements MBlockOption {

    private final EventPressState pressed;

    public PressedState(String pressed) {
        this.pressed = EventPressState.fromString(pressed);
    }

    public EventPressState getPressed() {
        return pressed;
    }

    public String getPressedState() {
        return pressed.getState();
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
        result[0] = pressed.getState();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PressedState)) return false;
        PressedState that = (PressedState) o;
        return pressed == that.pressed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressed);
    }

    public enum EventPressState {
        IS_PRESSED_TRUE("0"),
        IS_PRESSED_FALSE("1"),
        ;

        private final String state;

        EventPressState(String state) {
            this.state = Preconditions.checkNotNull(state);
        }

        public static EventPressState fromString(String state) {
            for (EventPressState f : values()) {
                if (f.getState().equals(state.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown PressedState: " + state);
        }

        public String getState() {
            return state;
        }
    }
}
