package io.github.ashwithpoojary98.clausses.columns;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawColumn extends AbstractColumn {

    private String expression;

    private Object[] bindings;

    public RawColumn() {

    }

    public RawColumn(String expression, Object[] bindings) {
        this.expression = expression;
        this.bindings = bindings;
    }

    @Override
    public AbstractClause copy() {
        RawColumn rawColumn = new RawColumn();
        rawColumn.setEngine(this.getEngine());
        rawColumn.setExpression(this.getExpression());
        rawColumn.setBindings(this.getBindings());
        rawColumn.setComponent(this.getComponent());
        return rawColumn;
    }
}
