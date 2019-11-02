package scratch.newast.model.expression;

import scratch.newast.model.graphiceffect.Color;

public class ColorTouches implements BoolExpr {
    private Color first;
    private Color second;

    public ColorTouches(Color first, Color second) {
        this.first = first;
        this.second = second;
    }

    public Color getFirst() {
        return first;
    }

    public void setFirst(Color first) {
        this.first = first;
    }

    public Color getSecond() {
        return second;
    }

    public void setSecond(Color second) {
        this.second = second;
    }
}