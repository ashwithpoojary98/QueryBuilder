package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetweenCondition<T> extends AbstractCondition {

    private String column;

    private T higher;

    private T lower;

    public BetweenCondition() {
    }

    public BetweenCondition(String column, boolean isOr, boolean isNot, T lower, T higher) {
        this.column = column;
        this.isOr = isOr;
        this.isNot = isNot;
        this.lower = lower;
        this.higher = higher;
    }

    @Override
    public AbstractClause copy() {
        BetweenCondition<T> betweenCondition = new BetweenCondition<>();
        betweenCondition.setEngine(this.getEngine());
        betweenCondition.setComponent(this.getComponent());
        betweenCondition.setNot(this.isNot);
        betweenCondition.setOr(this.isOr);
        betweenCondition.setColumn(this.column);
        betweenCondition.setHigher(this.higher);
        betweenCondition.setLower(this.lower);
        return betweenCondition;
    }
}
