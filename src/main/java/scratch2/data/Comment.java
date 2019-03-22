package scratch2.data;

import java.util.Arrays;

/**
 * Wrapper for Scratch Comments
 */
public class Comment {

    private double[] position;
    private String content;

    @Override
    public String toString() {
        return "Comment{" +
                "position=" + Arrays.toString(position) +
                ", content='" + content + '\'' +
                '}';
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
