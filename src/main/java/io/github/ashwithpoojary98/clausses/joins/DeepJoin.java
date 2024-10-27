package io.github.ashwithpoojary98.clausses.joins;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

@Getter
@Setter
public class DeepJoin extends AbstractJoin {

    private String type;

    private String expression;

    private String sourceKeySuffix;

    private String targetKey;

    private Function<String, String> sourceKeyGenerator;

    private Function<String, String> targetKeyGenerator;

    @Override
    public AbstractClause copy() {
        DeepJoin deepJoin = new DeepJoin();
        deepJoin.setEngine(this.getEngine());
        deepJoin.setComponent(this.getComponent());
        deepJoin.setType(this.type);
        deepJoin.setExpression(this.expression);
        deepJoin.setSourceKeySuffix(this.sourceKeySuffix);
        deepJoin.setTargetKey(this.targetKey);
        deepJoin.setSourceKeyGenerator(this.sourceKeyGenerator);
        deepJoin.setTargetKey(this.targetKey);
        return deepJoin;
    }
}
