package scratch.newast.model.costume;

import scratch.newast.model.expression.Number;

public class CostumeWithNum extends Costume {
    private Number num;

    public CostumeWithNum(Number num) {
        this.num = num;
    }

    public Number getNum() {
        return num;
    }

    public void setNum(Number num) {
        this.num = num;
    }
}