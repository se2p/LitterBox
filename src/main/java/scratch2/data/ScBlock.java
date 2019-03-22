package scratch2.data;

import java.util.List;

/**
 * Wrapper for Scratch Blocks
 */
public class ScBlock {

    private String content;
    private List<ScBlock> nestedBlocks;
    private List<ScBlock> elseBlocks;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n    [ScBlock{" + "content='").append(content).append('\'').append("}");
        if (nestedBlocks != null) {
            sb.append("\nnestedBlocks=");
            for (ScBlock b : getNestedBlocks()) {
                sb.append(b);
            }
        }
        if (elseBlocks != null) {
            sb.append("\nelseBlocks=");
            for (ScBlock b : getElseBlocks()) {
                sb.append(b);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public List<ScBlock> getNestedBlocks() {
        return nestedBlocks;
    }

    public void setNestedBlocks(List<ScBlock> nestedBlocks) {
        this.nestedBlocks = nestedBlocks;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ScBlock> getElseBlocks() {
        return elseBlocks;
    }

    public void setElseBlocks(List<ScBlock> elseBlocks) {
        this.elseBlocks = elseBlocks;
    }
}
