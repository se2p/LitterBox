package scratch.data;

import java.util.Arrays;

/**
 * Wrapper for Scratch Comments
 */
public class Comment {

    private double[] position;
    private String id;
    private String content;

    @Override
    public String toString() {
        return "Comment{ " +
                id +
                " position=" + Arrays.toString(position) +
                ", content='" + content + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
