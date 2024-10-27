package io.github.ashwithpoojary98.clausses.conditions;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractCondition extends AbstractClause {

    protected boolean isOr;

    protected boolean isNot;

}
