package io.github.ashwithpoojary98.clausses.joins;


import io.github.ashwithpoojary98.Join;
import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseJoin extends AbstractJoin {

    private Join join;

    public BaseJoin() {
    }

    public BaseJoin(Join join) {
        this.join = join;
    }

    @Override
    public AbstractClause copy() {
        BaseJoin baseJoin = new BaseJoin();
        baseJoin.setEngine(this.getEngine());
        baseJoin.setComponent(this.getComponent());
        baseJoin.setJoin(this.join.copy());
        return baseJoin;
    }
}
