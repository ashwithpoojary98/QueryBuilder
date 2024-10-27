package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BooleanCondition extends AbstractCondition {

    private String column;

    private boolean isValue;

    public BooleanCondition() {
    }

    public BooleanCondition(String column, boolean isValue, boolean isOr, boolean isNot) {
        this.column = column;
        this.isValue = isValue;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    public BooleanCondition(String column, boolean isValue) {
        this.column = column;
        this.isValue = isValue;
    }

    @Override
    public AbstractClause copy() {
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.setEngine(this.getEngine());
        booleanCondition.setComponent(this.getComponent());
        booleanCondition.setOr(this.isOr);
        booleanCondition.setNot(this.isNot);
        booleanCondition.setColumn(this.column);
        booleanCondition.setValue(this.isValue);
        return booleanCondition;
    }
}
