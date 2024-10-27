package io.github.ashwithpoojary98.clausses.conditions;


import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.BaseQuery;
import io.github.ashwithpoojary98.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubQueryCondition<Q extends BaseQuery<Q>> extends AbstractCondition {

    private Object value;

    private String operator;

    private Query query;


    public SubQueryCondition() {
    }

    public SubQueryCondition(Object value, String operator, Query query, boolean isOr, boolean isNot) {
        this.value = value;
        this.operator = operator;
        this.query = query;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        SubQueryCondition<Q> subQueryCondition = new SubQueryCondition<>();
        subQueryCondition.setEngine(this.getEngine());
        subQueryCondition.setComponent(this.getComponent());
        subQueryCondition.setOr(this.isOr);
        subQueryCondition.setNot(this.isNot);
        subQueryCondition.setValue(this.value);
        subQueryCondition.setOperator(this.operator);
        subQueryCondition.setQuery(this.query.copy());
        return subQueryCondition;
    }
}
