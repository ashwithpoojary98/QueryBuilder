package io.github.ashwithpoojary98.clausses.fromclausses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdHocTableFromClause extends AbstractFrom {

    private List<String> columns;

    private List<Object> values;

    public AdHocTableFromClause(){
    }
    public AdHocTableFromClause(String alias, List<String> columns, List<Object> values) {
        this.alias = alias;
        this.columns = columns;
        this.values = values;
    }

    @Override
    public AbstractClause copy() {
        AdHocTableFromClause adHocTableFromClause=new AdHocTableFromClause();
        adHocTableFromClause.setEngine(this.getEngine());
        adHocTableFromClause.setComponent(this.getComponent());
        adHocTableFromClause.setAlias(this.getAlias());
        adHocTableFromClause.setColumns(this.getColumns());
        adHocTableFromClause.setValues(this.getValues());
        return adHocTableFromClause;
    }
}
