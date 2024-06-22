package io.github.ashwithpoojary98.model;

import io.github.ashwithpoojary98.enums.JoinType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Join {

    private JoinType joinType;

    private String firstCondition;

    private String secondCondition;

    private String operator;

    private String secondTable;


}
