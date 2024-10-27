package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicStringCondition extends BasicCondition {

    private boolean isCaseSensitive;

    private String escapeCharacter;

    public BasicStringCondition(){}

    public BasicStringCondition(String operator,String column,Object value,boolean isCaseSensitive,String escapeCharacter,boolean isOr,boolean isNot){
        this.setOperator(operator);
        this.setColumn(column);
        this.setValue(value);
        this.setCaseSensitive(isCaseSensitive);
        this.setEscapeCharacter(escapeCharacter);
        this.isOr=isOr;
        this.isNot=isNot;
    }

    @Override
    public AbstractClause copy() {
        BasicStringCondition basicStringCondition = new BasicStringCondition();
        basicStringCondition.setEngine(this.getEngine());
        basicStringCondition.setColumn(this.getColumn());
        basicStringCondition.setOperator(this.getOperator());
        basicStringCondition.setValue(this.getValue());
        basicStringCondition.setOr(this.isOr);
        basicStringCondition.setNot(this.isNot);
        basicStringCondition.setComponent(this.getComponent());
        basicStringCondition.setCaseSensitive(this.isCaseSensitive);
        basicStringCondition.setEscapeCharacter(this.escapeCharacter);
        return basicStringCondition;
    }
}
