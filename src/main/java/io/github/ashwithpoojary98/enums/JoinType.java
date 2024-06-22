package io.github.ashwithpoojary98.enums;

import io.github.ashwithpoojary98.model.Join;
import lombok.Getter;

@Getter
public enum JoinType {

    INNER_JOIN("INNER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN"),
    FULL_JOIN("FULL JOIN"),
    CROSS_JOIN("CROSS JOIN");

    private final String joinTypeValue;

    JoinType(String joinTypeValue) {
        this.joinTypeValue = joinTypeValue;
    }
}
