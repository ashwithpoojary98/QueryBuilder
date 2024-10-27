package io.github.ashwithpoojary98.clausses.orderbyclauses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawOrderBy extends AbstractOrderBy {

    private String expression;

    private Object[] bindings;

    public RawOrderBy() {

    }

    public RawOrderBy(String expression, Object[] bindings) {
        this.expression = expression;
        this.bindings = bindings;
    }


    @Override
    public AbstractClause copy() {
        RawOrderBy rawOrderBy = new RawOrderBy();
        rawOrderBy.setEngine(this.getEngine());
        rawOrderBy.setComponent(this.getComponent());
        rawOrderBy.setExpression(this.expression);
        rawOrderBy.setBindings(this.bindings);
        return rawOrderBy;
    }
}
