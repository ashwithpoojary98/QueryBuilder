package io.github.ashwithpoojary98.clausses.conditions;


import io.github.ashwithpoojary98.clausses.AbstractClause;
import io.github.ashwithpoojary98.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExistsCondition extends AbstractCondition {

    private Query query;

    public ExistsCondition() {
    }

    public ExistsCondition(Query query, boolean isOr, boolean isNot) {
        this.query = query;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.setEngine(this.getEngine());
        existsCondition.setComponent(this.getComponent());
        existsCondition.setOr(this.isOr);
        existsCondition.setNot(this.isNot);
        existsCondition.setQuery(this.query.copy());
        return existsCondition;
    }
}
