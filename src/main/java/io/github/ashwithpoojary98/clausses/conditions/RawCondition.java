package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawCondition extends AbstractCondition {

    private String expression;

    private Object[] bindings;

    public RawCondition() {
    }

    public RawCondition(String expression, Object[] binding, boolean isOr, boolean isNot) {
        this.expression = expression;
        this.bindings = binding;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        RawCondition rawCondition = new RawCondition();
        rawCondition.setEngine(this.getEngine());
        rawCondition.setComponent(this.getComponent());
        rawCondition.setOr(this.isOr);
        rawCondition.setNot(this.isNot);
        rawCondition.setExpression(this.expression);
        rawCondition.setBindings(this.bindings);
        return rawCondition;
    }
}
