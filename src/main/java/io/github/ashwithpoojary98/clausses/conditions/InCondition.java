package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InCondition<T> extends AbstractCondition {

    private String column;

    private List<T> values;

    public InCondition() {
    }

    public InCondition(String column, boolean isOr, boolean isNot, List<T> values) {
        this.column = column;
        this.isOr = isOr;
        this.isNot = isNot;
        this.values = values;
    }

    @Override
    public AbstractClause copy() {
        InCondition<T> inCondition = new InCondition<>();
        inCondition.setEngine(this.getEngine());
        inCondition.setComponent(this.getComponent());
        inCondition.setOr(this.isOr);
        inCondition.setNot(this.isNot);
        inCondition.setColumn(this.column);
        inCondition.setValues(this.getValues());
        return inCondition;
    }
}
