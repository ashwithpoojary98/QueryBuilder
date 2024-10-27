package io.github.ashwithpoojary98.clausses.fromclausses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FromClause extends AbstractFrom {

    private String table;

    public FromClause(){}

    public FromClause(String table){
        this.table=table;
    }
    @Override
    public AbstractClause copy() {
        FromClause fromClause=new FromClause();
        fromClause.setEngine(this.getEngine());
        fromClause.setComponent(this.getComponent());
        fromClause.setAlias(this.alias);
        fromClause.setTable(this.table);
        return fromClause;
    }
}
