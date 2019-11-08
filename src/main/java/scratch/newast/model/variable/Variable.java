package scratch.newast.model.variable;

import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.list.ListExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;

public interface Variable extends BoolExpr, NumExpr, StringExpr, ListExpr {

}