package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoColumnsCondition extends AbstractCondition {
    private String first;
    private String operator;
    private String second;


    public TwoColumnsCondition() {
    }

    public TwoColumnsCondition(String first, String second, String operator, boolean isOr, boolean isNot) {
        super.setOr(isOr);
        super.setNot(isNot);
        this.first = first;
        this.second = second;
        this.operator = operator;

    }

    @Override
    public AbstractClause copy() {
        TwoColumnsCondition twoColumnsCondition = new TwoColumnsCondition();
        twoColumnsCondition.setEngine(this.getEngine());
        twoColumnsCondition.setComponent(this.getComponent());
        twoColumnsCondition.setOr(this.isOr);
        twoColumnsCondition.setNot(this.isNot);
        twoColumnsCondition.setFirst(this.first);
        twoColumnsCondition.setSecond(this.second);
        twoColumnsCondition.setOperator(this.operator);
        return twoColumnsCondition;
    }
}
