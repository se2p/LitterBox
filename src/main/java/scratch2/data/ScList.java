package scratch2.data;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for Scratch Lists
 */
public class ScList {

    private String name;
    private List<String> content;
    private int[] position;
    private boolean visible;

    @Override
    public String toString() {
        return "ScList{" +
                "name='" + name + '\'' +
                ", content=" + content +
                ", position=" + Arrays.toString(position) +
                ", visible=" + visible +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
