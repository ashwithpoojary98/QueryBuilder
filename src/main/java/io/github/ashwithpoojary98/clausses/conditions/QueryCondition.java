package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.BaseQuery;
import io.github.ashwithpoojary98.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryCondition<Q extends BaseQuery<Q>> extends AbstractCondition {

    private String column;
    private String operator;
    private Query query;

    public QueryCondition() {
    }

    public QueryCondition(String column, String operator, Query query, boolean isOr, boolean isNot) {
        this.column = column;
        this.operator = operator;
        this.query = query;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        QueryCondition<Q> queryCondition = new QueryCondition<>();
        queryCondition.setEngine(this.getEngine());
        queryCondition.setComponent(this.getComponent());
        queryCondition.setOr(this.isOr);
        queryCondition.setNot(this.isNot);
        queryCondition.setColumn(this.column);
        queryCondition.setOperator(this.operator);
        queryCondition.setQuery(this.query.copy());
        return queryCondition;
    }
}
