package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicDateCondition extends BasicCondition {
    private String part;

    public BasicDateCondition() {
    }

    public BasicDateCondition(String operator, String column, Object value, String part, boolean isOr, boolean isNot) {
        this.setOperator(operator);
        this.setColumn(column);
        this.setValue(value);
        this.part = part;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        BasicDateCondition basicDateCondition = new BasicDateCondition();
        basicDateCondition.setEngine(this.getEngine());
        basicDateCondition.setColumn(this.getColumn());
        basicDateCondition.setOperator(this.getOperator());
        basicDateCondition.setValue(this.getValue());
        basicDateCondition.setOr(this.isOr);
        basicDateCondition.setNot(this.isNot);
        basicDateCondition.setComponent(this.getComponent());
        basicDateCondition.setPart(this.part);
        return basicDateCondition;
    }
}
