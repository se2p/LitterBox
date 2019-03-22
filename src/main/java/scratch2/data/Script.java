package scratch2.data;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for Scratch Scripts
 */
public class Script {

    private double[] position;
    private List<ScBlock> blocks;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nScript{" + "position=").append(Arrays.toString(position)).append(", blocks:");
        for (ScBlock b : blocks) {
            sb.append(b.toString());
        }
        sb.append("\n");
        return sb.toString();
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public List<ScBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ScBlock> blocks) {
        this.blocks = blocks;
    }

}
