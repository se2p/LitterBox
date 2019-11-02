package scratch.newast.model.backdrop;

import scratch.newast.model.expression.num.Number;

public class BackdropWithNum implements Backdrop {
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