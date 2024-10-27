package io.github.ashwithpoojary98.clausses.combines;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawCombine extends AbstractCombine {

    private String expression;

    private Object[] bindings;

    public RawCombine() {
    }

    public RawCombine(String expression, Object[] bindings) {
        this.expression = expression;
        this.bindings = bindings;
    }

    @Override
    public AbstractClause copy() {
        RawCombine rawCombine = new RawCombine();
        rawCombine.setEngine(this.getEngine());
        rawCombine.setComponent(this.getComponent());
        rawCombine.setExpression(this.expression);
        rawCombine.setBindings(this.bindings);
        return rawCombine;
    }
}
