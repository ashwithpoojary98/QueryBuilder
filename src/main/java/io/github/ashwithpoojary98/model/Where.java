package io.github.ashwithpoojary98.model;

import io.github.ashwithpoojary98.enums.Operator;
import lombok.Data;

import java.util.Arrays;

@Data
public class Where {
    private String columnName;
    private String conditionalOperator;
    private Object value;
    private Operator operator;
    private Object[] multiValue;

    public Where(String columnName, String conditionalOperator, Object value, Operator operator) {
        this.columnName = columnName;
        this.conditionalOperator = conditionalOperator;
        this.value = value;
        this.operator = operator;
    }

    public Where(String columnName, String conditionalOperator, Object[] multiValue, Operator operator) {
        this.columnName = columnName;
        this.conditionalOperator = conditionalOperator;
        this.multiValue = multiValue;
        this.operator = operator;
    }
}
