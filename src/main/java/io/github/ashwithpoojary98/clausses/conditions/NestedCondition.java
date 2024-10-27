package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.BaseQuery;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NestedCondition<T extends BaseQuery<T>> extends AbstractCondition {

    private T query;

    public NestedCondition() {
    }

    public NestedCondition(T query, boolean isOr, boolean isNot) {
        this.query = query;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        NestedCondition<T> nestedCondition = new NestedCondition<>();
        nestedCondition.setEngine(this.getEngine());
        nestedCondition.setComponent(this.getComponent());
        nestedCondition.setOr(this.isOr);
        nestedCondition.setNot(this.isNot);
        nestedCondition.setQuery(query.copy());
        return nestedCondition;
    }
}
