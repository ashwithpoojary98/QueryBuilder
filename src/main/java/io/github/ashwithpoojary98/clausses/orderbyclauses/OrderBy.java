package io.github.ashwithpoojary98.clausses.orderbyclauses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderBy extends AbstractOrderBy {

    private String column;

    private boolean isAscending = true;

    public OrderBy() {
    }

    public OrderBy(String column, boolean isAscending) {
        this.column = column;
        this.isAscending = isAscending;
    }

    @Override
    public AbstractClause copy() {
        OrderBy orderBy = new OrderBy();
        orderBy.setEngine(this.getEngine());
        orderBy.setComponent(this.getComponent());
        orderBy.setColumn(this.column);
        orderBy.setAscending(this.isAscending);
        return orderBy;
    }
}
