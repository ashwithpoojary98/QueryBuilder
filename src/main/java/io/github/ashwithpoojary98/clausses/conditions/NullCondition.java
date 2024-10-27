package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NullCondition extends AbstractCondition {

    private String column;

    public NullCondition(){}

    public NullCondition(String column, boolean isOr, boolean isNot) {
        this.column = column;
        this.isOr = isOr;
        this.isNot = isNot;
    }

    @Override
    public AbstractClause copy() {
        NullCondition nullCondition = new NullCondition();
        nullCondition.setEngine(this.getEngine());
        nullCondition.setComponent(this.getComponent());
        nullCondition.setOr(this.isOr);
        nullCondition.setNot(this.isNot);
        nullCondition.setColumn(this.column);
        return nullCondition;
    }
}
