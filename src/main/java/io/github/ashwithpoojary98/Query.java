package io.github.ashwithpoojary98;

import io.github.ashwithpoojary98.clause.IJoin;
import io.github.ashwithpoojary98.clause.ISelect;
import io.github.ashwithpoojary98.clause.IWhere;
import io.github.ashwithpoojary98.enums.JoinType;
import io.github.ashwithpoojary98.enums.Operator;
import io.github.ashwithpoojary98.model.Join;
import io.github.ashwithpoojary98.model.Where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Query implements IWhere, ISelect, IJoin {

    private String table;
    private final List<String> columnName = new ArrayList<>();
    private final List<Where> whereCondition = new ArrayList<>();
    private final List<Join> joinCondition = new ArrayList<>();
    private long limit = 0;
    private long offset = 0;
    private String orderByColumn;
    private String orderType;


    public Query() {
    }

    public Query(String table) {
        this.table = table;
    }


    public Query from(String table) {
        this.table = table;
        return this;
    }

    public Query limit(long limit) {
        this.limit = limit;
        return this;
    }

    public Query offset(long offset) {
        this.offset = offset;
        return this;
    }

    public Query orderBy(String orderByColumn) {
        this.orderByColumn = orderByColumn;
        return this;
    }

    public Query orderBy(String orderByColumn, String orderType) {
        this.orderByColumn = orderByColumn;
        this.orderType = orderType;
        return this;
    }

    @Override
    public Query where(String column, String op, Object value) {
        whereCondition.add(new Where(column, op, value, Operator.AND));
        return this;
    }

    @Override
    public Query whereNot(String column, String op, Object value) {
        whereCondition.add(new Where(column, op, value, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhere(String column, String op, Object value) {
        whereCondition.add(new Where(column, op, value, Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNot(String column, String op, Object value) {
        whereCondition.add(new Where(column, op, value, Operator.OR_NOT));
        return this;
    }

    @Override
    public Query where(String column, Object value) {
        whereCondition.add(new Where(column, "=", value, Operator.AND));
        return this;
    }

    @Override
    public Query whereNot(String column, Object value) {
        whereCondition.add(new Where(column, "=", value, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhere(String column, Object value) {
        whereCondition.add(new Where(column, "=", value, Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNot(String column, Object value) {
        whereCondition.add(new Where(column, "=", value, Operator.OR_NOT));
        return this;
    }


    @Override
    public Query whereNull(String column) {
        whereCondition.add(new Where(column, "IS", "NULL", Operator.AND));
        return this;
    }

    @Override
    public Query whereNotNull(String column) {
        whereCondition.add(new Where(column, "IS", "NULL", Operator.NOT));
        return this;
    }

    @Override
    public Query orWhereNull(String column) {
        whereCondition.add(new Where(column, "IS", "NULL", Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNotNull(String column) {
        whereCondition.add(new Where(column, "IS", "NULL", Operator.OR_NOT));
        return this;
    }

    @Override
    public Query whereTrue(String column) {
        whereCondition.add(new Where(column, "=", "TRUE", Operator.AND));
        return this;
    }

    @Override
    public Query orWhereTrue(String column) {
        whereCondition.add(new Where(column, "=", "TRUE", Operator.OR));
        return this;
    }

    @Override
    public Query whereFalse(String column) {
        whereCondition.add(new Where(column, "=", "FALSE", Operator.AND));
        return this;
    }

    @Override
    public Query orWhereFalse(String column) {
        whereCondition.add(new Where(column, "=", "FALSE", Operator.OR));
        return this;
    }

    @Override
    public Query whereLike(String column, Object value) {
        whereCondition.add(new Where(column, "LIKE", value, Operator.AND));
        return this;
    }

    @Override
    public Query whereNotLike(String column, Object value) {
        whereCondition.add(new Where(column, "LIKE", value, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhereLike(String column, Object value) {
        whereCondition.add(new Where(column, "LIKE", value, Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNotLike(String column, Object value) {
        whereCondition.add(new Where(column, "LIKE", value, Operator.OR_NOT));
        return this;
    }

    @Override
    public Query whereStarts(String column, Object value) {
        whereCondition.add(new Where(column, "START", value, Operator.AND));
        return this;
    }

    @Override
    public Query whereNotStarts(String column, Object value) {
        whereCondition.add(new Where(column, "START", value, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhereStarts(String column, Object value) {
        whereCondition.add(new Where(column, "START", value, Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNotStarts(String column, Object value) {
        whereCondition.add(new Where(column, "START", value, Operator.OR_NOT));
        return this;
    }

    @Override
    public Query whereEnds(String column, Object value) {
        whereCondition.add(new Where(column, "END", value, Operator.AND));
        return this;
    }

    @Override
    public Query whereNotEnds(String column, Object value) {
        whereCondition.add(new Where(column, "END", value, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhereEnds(String column, Object value) {
        whereCondition.add(new Where(column, "END", value, Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNotEnds(String column, Object value) {
        whereCondition.add(new Where(column, "END", value, Operator.OR_NOT));
        return this;
    }

    @Override
    public Query whereContains(String column, Object value) {
        whereCondition.add(new Where(column, "CONTAINS", value, Operator.AND));
        return this;
    }

    @Override
    public Query whereNotContains(String column, Object value) {
        whereCondition.add(new Where(column, "CONTAINS", value, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhereContains(String column, Object value) {
        whereCondition.add(new Where(column, "CONTAINS", value, Operator.OR));
        return this;
    }

    @Override
    public Query orWhereNotContains(String column, Object value) {
        whereCondition.add(new Where(column, "CONTAINS", value, Operator.OR_NOT));
        return this;
    }

    @Override
    public Query whereIn(String column, Object... values) {
        for (Object value : values) {
            whereCondition.add(new Where(column, "IN", value, Operator.AND));
        }
        return this;
    }

    @Override
    public Query orWhereIn(String column, Object... values) {
        for (Object value : values) {
            whereCondition.add(new Where(column, "IN", value, Operator.OR));
        }
        return this;
    }

    @Override
    public Query whereNotIn(String column, Object... values) {
        whereCondition.add(new Where(column, "IN", values, Operator.NOT));
        return this;
    }

    @Override
    public Query orWhereNotIn(String column, Object... values) {
        whereCondition.add(new Where(column, "IN", values, Operator.OR_NOT));
        return this;
    }

    @Override
    public Query join(String table, String first, String second, String op) {
        joinCondition.add(new Join(JoinType.INNER_JOIN, second, first, op, table));
        return this;
    }

    @Override
    public Query join(String tableName, String first, String second) {
        joinCondition.add(new Join(JoinType.INNER_JOIN, second, first, "=", tableName));
        return this;
    }

    @Override
    public Query leftJoin(String tableName, String first, String second, String op) {
        joinCondition.add(new Join(JoinType.LEFT_JOIN, second, first, op, tableName));
        return this;
    }

    @Override
    public Query leftJoin(String tableName, String first, String second) {
        joinCondition.add(new Join(JoinType.LEFT_JOIN, second, first, "=", tableName));
        return this;
    }

    @Override
    public Query rightJoin(String tableName, String first, String second, String op) {
        joinCondition.add(new Join(JoinType.RIGHT_JOIN, second, first, op, tableName));
        return this;
    }

    @Override
    public Query rightJoin(String tableName, String first, String second) {
        joinCondition.add(new Join(JoinType.RIGHT_JOIN, second, first, "=", tableName));
        return this;
    }

    @Override
    public Query crossJoin(String tableName, String first, String second, String op) {
        joinCondition.add(new Join(JoinType.CROSS_JOIN, second, first, op, tableName));
        return this;
    }

    @Override
    public Query crossJoin(String tableName, String first, String second) {
        joinCondition.add(new Join(JoinType.CROSS_JOIN, second, first, "=", tableName));
        return this;
    }

    @Override
    public Query select(String... columns) {
        Collections.addAll(columnName, columns);
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (columnName.isEmpty()) {
            sb.append("*");
        } else {
            sb.append(columnName.stream()
                    .map(this::escapeColumnName)
                    .collect(Collectors.joining(", ")));
        }
        sb.append(" FROM ").append("\"").append(table).append("\"").append(" ");
        sb.append(buildJoinClause()).append(" ");
        sb.append(buildWhereClause());
        if (orderByColumn != null) {
            if (orderType != null) {
                sb.append(" ORDER BY ").append(escapeColumnName(orderByColumn)).append(" ").append(orderByColumn);
            } else {
                sb.append(" ORDER BY ").append(escapeColumnName(orderByColumn));
            }

        }
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }
        if (offset > 0) {
            sb.append(" OFFSET").append(offset);
        }
        return sb.toString();
    }


    private String buildWhereClause() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("WHERE");
        boolean isFirstFound = true;

        for (Where where : whereCondition) {
            stringBuilder.append(getWhereQuery(where, isFirstFound));
            isFirstFound = false;
        }
        return stringBuilder.toString();
    }

    private String getWhereQuery(Where where, boolean isFirst) {
        String column = where.getColumnName();
        Object value = where.getValue();
        String condition = where.getConditionalOperator();
        Operator operator = where.getOperator();
        Object[] multipleValue = where.getMultiValue();
        StringBuilder stringBuilder = new StringBuilder();
        if (where.getMultiValue() != null) {
            for (Object val : multipleValue) {
                stringBuilder.append("'").append(val).append("'").append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        if (condition.equalsIgnoreCase("CONTAINS")) {
            value = "= ('%" + value + "%')";
        } else if (condition.equalsIgnoreCase("START")) {
            value = "= ('" + value + "%')";
        } else if ((condition.equalsIgnoreCase("END"))) {
            value = "= ('%" + value + "')";
        } else {
            if (value != null && (value.toString().equalsIgnoreCase("NULL") || value.toString().equalsIgnoreCase("TRUE") || value.toString().equalsIgnoreCase("FALSE"))) {
                if (!condition.equalsIgnoreCase("IS")) {
                    value = condition + " " + value;
                }
            } else {
                value = condition + " ('" + value + "')";
            }

        }
        if (!isFirst) {
            if (multipleValue != null) {
                if (operator == Operator.OR_NOT) {
                    return " OR " + escapeColumnName(column) +
                            " NOT " + condition + " (" + stringBuilder + ")";
                } else if (operator == Operator.NOT) {
                    return " AND " + escapeColumnName(column) +
                            " NOT " + condition + " (" + stringBuilder + ")";
                } else {
                    return " " + operator.toString() + " " + escapeColumnName(column) + " " +
                            condition + " (" + stringBuilder + ")";
                }
            } else {
                if (operator == Operator.OR_NOT) {
                    if (condition.equalsIgnoreCase("IS")) {
                        return " OR " + escapeColumnName(column) +
                                " IS NOT " + value;
                    } else {
                        return " OR " + escapeColumnName(column) +
                                " NOT " + value;
                    }

                } else if (operator == Operator.NOT) {
                    if (condition.equalsIgnoreCase("IS")) {
                        return " AND " + escapeColumnName(column) +
                                " IS NOT " + value;
                    } else {
                        return " AND " + escapeColumnName(column) +
                                " NOT " + value;
                    }
                } else {
                    if (condition.equalsIgnoreCase("IS")) {
                        return " AND " + escapeColumnName(column) +
                                " IS " + value;
                    }
                    return " " + operator.toString() + " " + escapeColumnName(column) + " " + value;
                }
            }
        } else {
            if (multipleValue != null) {
                return " \"" + column + "\" " + condition + " (" + stringBuilder + ")";
            }
            return " " + escapeColumnName(column) + " " + value;
        }
    }

    private String escapeColumnName(String columnName) {
        // Handle dot notation
        return columnName.contains(".") ?
                String.format("\"%s\".\"%s\"", columnName.split("\\.")) :
                String.format("\"%s\"", columnName);
    }

    private String buildJoinClause() {
        return joinCondition.stream()
                .map(this::createJoinClause)
                .collect(Collectors.joining(" "));
    }

    private String createJoinClause(Join join) {
        return join.getJoinType().getJoinTypeValue() + " \"" + join.getSecondTable() + "\" ON "
                + escapeColumnName(join.getFirstCondition()) + " " + join.getOperator() + " "
                + escapeColumnName(join.getSecondCondition());
    }
}