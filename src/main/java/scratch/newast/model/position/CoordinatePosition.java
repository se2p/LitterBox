package scratch.newast.model.position;

import scratch.newast.model.expression.num.Number;

public class CoordinatePosition implements Position {
    private Number xCoord;
    private Number yCoord;

    public CoordinatePosition(Number xCoord, Number yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    public Number getXCoord() {
        return xCoord;
    }

    public void setXCoord(Number xCoord) {
        this.xCoord = xCoord;
    }

    public Number getYCoord() {
        return yCoord;
    }

    public void setYCoord(Number yCoord) {
        this.yCoord = yCoord;
    }
}