package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicCondition extends AbstractCondition {

    private String column;

    private String operator;

    private Object value;

    public BasicCondition() {
    }

    public BasicCondition(String column, String operator, Object value, boolean isOr, boolean isNot) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        BasicCondition basicCondition = new BasicCondition();
        basicCondition.setEngine(this.getEngine());
        basicCondition.setColumn(this.column);
        basicCondition.setOperator(this.operator);
        basicCondition.setValue(this.value);
        basicCondition.setOr(this.isOr);
        basicCondition.setNot(this.isNot);
        basicCondition.setComponent(this.getComponent());
        return basicCondition;
    }
}
