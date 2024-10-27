package io.github.ashwithpoojary98.clausses.conditions;


import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InQueryCondition extends AbstractCondition {

    private Query query;

    private String column;

    public InQueryCondition() {

    }

    public InQueryCondition(String column, boolean isOr, boolean isNot, Query query) {
        this.column = column;
        this.isOr = isOr;
        this.isNot = isNot;
        this.query = query;
    }

    @Override
    public AbstractClause copy() {
        InQueryCondition inQueryCondition = new InQueryCondition();
        inQueryCondition.setEngine(this.getEngine());
        inQueryCondition.setComponent(this.getComponent());
        inQueryCondition.setOr(this.isOr);
        inQueryCondition.setNot(this.isNot);
        inQueryCondition.setQuery(this.query.copy());
        inQueryCondition.setColumn(this.column);
        return inQueryCondition;
    }
}
