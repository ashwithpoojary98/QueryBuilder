package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.SqlResult;
import io.github.ashwithpoojary98.clausses.conditions.BasicDateCondition;
import io.github.ashwithpoojary98.clausses.insertclauses.InsertClause;
import io.github.ashwithpoojary98.Query;

import java.util.List;

public class OracleCompiler extends Compiler {

    private boolean useLegacyPagination;

    public boolean getUseLegacyPagination() {
        return this.useLegacyPagination;
    }

    public void setUseLegacyPagination(boolean useLegacyPagination) {
        this.useLegacyPagination = useLegacyPagination;
    }

    public OracleCompiler() {
        columnAsKeyword = "";
        tableAsKeyword = "";
        parameterPrefix = ":p";
        multiInsertStartClause = "INSERT ALL INTO";
        singleRowDummyTableName = "DUAL";
        engineCode = EngineCodes.ORACLE.getCode();
    }


    @Override
    protected SqlResult compileSelectQuery(Query query) {
        if (!useLegacyPagination) {
            return super.compileSelectQuery(query);
        }

        SqlResult result = super.compileSelectQuery(query);

        applyLegacyLimit(result);

        return result;
    }

    @Override
    public String compileLimit(SqlResult ctx) {
        if (useLegacyPagination) {
            // In pre-12c versions of Oracle, limit is handled by ROWNUM techniques
            return null;
        }

        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit == 0 && offset == 0) {
            return null;
        }

        String safeOrder = "";

        if (!ctx.getQuery().hasComponent("order")) {
            safeOrder = "ORDER BY (SELECT 0 FROM DUAL) ";
        }

        if (limit == 0) {
            ctx.getBindings().add(offset);
            return safeOrder + "OFFSET " + parameterPlaceholder + " ROWS";
        }

        ctx.getBindings().add(offset);
        ctx.getBindings().add(limit);

        return safeOrder + "OFFSET " + parameterPlaceholder + " ROWS FETCH NEXT " + parameterPlaceholder + " ROWS ONLY";
    }

    protected String compileBasicDateCondition(SqlResult ctx, BasicDateCondition condition) {
        String column = wrap(condition.getColumn());
        String value = parameter(ctx, condition.getValue());

        String sql = "";
        String valueFormat = "";

        boolean isDateTime = condition.getValue() instanceof java.util.Date;

        switch (condition.getPart()) {
            case "date": // assume YY-MM-DD format
                if (isDateTime) {
                    valueFormat = value;
                } else {
                    valueFormat = "TO_DATE(" + value + ", 'YY-MM-DD')";
                }
                sql = "TO_CHAR(" + column + ", 'YY-MM-DD') " + condition.getOperator() + " TO_CHAR(" + valueFormat + ", 'YY-MM-DD')";
                break;
            case "time":
                if (isDateTime) {
                    valueFormat = value;
                } else {
                    // assume HH:MM format
                    if (condition.getValue().toString().split(":").length == 2) {
                        valueFormat = "TO_DATE(" + value + ", 'HH24:MI')";
                    } else { // assume HH:MM:SS format
                        valueFormat = "TO_DATE(" + value + ", 'HH24:MI:SS')";
                    }
                }
                sql = "TO_CHAR(" + column + ", 'HH24:MI:SS') " + condition.getOperator() + " TO_CHAR(" + valueFormat + ", 'HH24:MI:SS')";
                break;
            case "year":
            case "month":
            case "day":
            case "hour":
            case "minute":
            case "second":
                sql = "EXTRACT(" + condition.getPart().toUpperCase() + " FROM " + column + ") " + condition.getOperator() + " " + value;
                break;
            default:
                sql = column + " " + condition.getOperator() + " " + value;
                break;
        }

        if (condition.isNot()) {
            return "NOT (" + sql + ")";
        }

        return sql;
    }


    void applyLegacyLimit(SqlResult ctx) {
        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit == 0 && offset == 0) {
            return;
        }

        String newSql;
        if (limit == 0) {
            newSql = String.format(
                    "SELECT * FROM (SELECT \"results_wrapper\".*, ROWNUM \"row_num\" FROM (%s) \"results_wrapper\") WHERE \"row_num\" > %s",
                    ctx.getRawSql(), parameterPlaceholder);
            ctx.getBindings().add(offset);
        } else if (offset == 0) {
            newSql = String.format(
                    "SELECT * FROM (%s) WHERE ROWNUM <= %s",
                    ctx.getRawSql(), parameterPlaceholder);
            ctx.getBindings().add(limit);
        } else {
            newSql = String.format(
                    "SELECT * FROM (SELECT \"results_wrapper\".*, ROWNUM \"row_num\" FROM (%s) \"results_wrapper\" WHERE ROWNUM <= %s) WHERE \"row_num\" > %s",
                    ctx.getRawSql(), parameterPlaceholder, parameterPlaceholder);
            ctx.getBindings().add(limit + offset);
            ctx.getBindings().add(offset);
        }

        ctx.setRawSql(newSql);
    }

    protected SqlResult compileRemainingInsertClauses(SqlResult ctx, String table, List<InsertClause> inserts) {
        // Skip the first insert clause and process the rest
        for (InsertClause insert : inserts.subList(1, inserts.size())) {
            String columns = getInsertColumnsList(insert.getColumns());
            String values = String.join(", ", parameterize(ctx, insert.getValues()));

            String intoFormat = " INTO %s%s VALUES (%s)";
            String nextInsert = String.format(intoFormat, table, columns, values);

            ctx.setRawSql(ctx.getRawSql() + nextInsert);
        }

        ctx.setRawSql(ctx.getRawSql() + " SELECT 1 FROM DUAL");
        return ctx;
    }


}
