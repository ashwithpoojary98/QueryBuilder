package io.github.ashwithpoojary98.clause;

import io.github.ashwithpoojary98.Query;

import java.util.List;

public interface IWhere {

    Query where(String column, String op, Object value);

    Query whereNot(String column, String op, Object value);

    Query orWhere(String column, String op, Object value);

    Query orWhereNot(String column, String op, Object value);

    Query where(String column, Object value);

    Query whereNot(String column, Object value);

    Query orWhere(String column, Object value);

    Query orWhereNot(String column, Object value);

    Query whereNull(String column);

    Query whereNotNull(String column);

    Query orWhereNull(String column);

    Query orWhereNotNull(String column);

    Query whereTrue(String column);

    Query orWhereTrue(String column);

    Query whereFalse(String column);

    Query orWhereFalse(String column);

    Query whereLike(String column, Object value);

    Query whereNotLike(String column, Object value);

    Query orWhereLike(String column, Object value);

    Query orWhereNotLike(String column, Object value);

    Query whereStarts(String column, Object value);

    Query whereNotStarts(String column, Object value);

    Query orWhereStarts(String column, Object value);

    Query orWhereNotStarts(String column, Object value);

    Query whereEnds(String column, Object value);

    Query whereNotEnds(String column, Object value);

    Query orWhereEnds(String column, Object value);

    Query orWhereNotEnds(String column, Object value);

    Query whereContains(String column, Object value);

    Query whereNotContains(String column, Object value);

    Query orWhereContains(String column, Object value);

    Query orWhereNotContains(String column, Object value);

    Query whereIn(String column, Object... values);

    Query orWhereIn(String column, Object... values);

    Query whereNotIn(String column, Object... values);

    Query orWhereNotIn(String column, Object... values);


}
