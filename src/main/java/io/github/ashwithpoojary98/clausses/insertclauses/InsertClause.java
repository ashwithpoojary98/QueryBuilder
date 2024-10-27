package io.github.ashwithpoojary98.clausses.insertclauses;

import io.github.ashwithpoojary98.clausses.AbstractClause;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InsertClause extends AbstractInsertClause {

    private List<String> columns = new ArrayList<>();

    private List<Object> values = new ArrayList<>();

    private boolean isReturnId;

    public InsertClause() {
    }

    public InsertClause(List<String> columns, List<Object> values, boolean isReturnId) {
        this.columns = columns;
        this.values = values;
        this.isReturnId = isReturnId;
    }

    public InsertClause(List<String> columns, List<Object> values) {
        this.columns = columns;
        this.values = values;
    }

    @Override
    public AbstractClause copy() {
        InsertClause insertClause = new InsertClause();
        insertClause.setEngine(this.getEngine());
        insertClause.setComponent(this.getComponent());
        insertClause.setColumns(this.columns);
        insertClause.setValues(this.values);
        insertClause.setReturnId(this.isReturnId);
        return insertClause;
    }
}
