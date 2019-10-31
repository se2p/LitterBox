package scratch.newast.model.backdrop;

import scratch.newast.model.expression.Number;

public class BackdropWithNum extends Backdrop {
    private Number num;

    public BackdropWithNum(Number num) {
        this.num = num;
    }

    public Number getNum() {
        return num;
    }

    public void setNum(Number num) {
        this.num = num;
    }
}