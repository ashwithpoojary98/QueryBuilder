package io.github.ashwithpoojary98.clausses.fromclausses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawFromClause extends AbstractFrom {

    private String expression;

    private Object[] bindings;

    public RawFromClause() {
    }

    public RawFromClause(String alias, String expression, Object[] bindings) {
        this.alias = alias;
        this.expression = expression;
        this.bindings = bindings;
    }

    public RawFromClause(String expression, Object[] bindings) {
        this.expression = expression;
        this.bindings = bindings;
    }

    @Override
    public AbstractClause copy() {
        RawFromClause rawFromClause = new RawFromClause();
        rawFromClause.setEngine(this.getEngine());
        rawFromClause.setComponent(this.getComponent());
        rawFromClause.setAlias(this.alias);
        rawFromClause.setExpression(this.expression);
        rawFromClause.setBindings(this.bindings);
        return rawFromClause;
    }
}
