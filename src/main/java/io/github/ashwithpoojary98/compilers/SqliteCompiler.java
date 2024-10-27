package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.SqlResult;
import io.github.ashwithpoojary98.clausses.conditions.BasicDateCondition;

import java.util.HashMap;
import java.util.Map;

public class SqliteCompiler extends Compiler {

    public SqliteCompiler() {
        engineCode = EngineCodes.SQLITE.getCode();
        openingIdentifier = "\"";
        closingIdentifier = "\"";
        lastId = "select last_insert_rowid() as id";
        setSupportsFilterClause(true);
    }

    @Override
    public String compileTrue() {
        return "1";
    }

    @Override
    public String compileFalse() {
        return "0";
    }

    @Override
    public String compileLimit(SqlResult ctx) {
        int limit = ctx.getQuery().getLimit(engineCode);
        int offset = ctx.getQuery().getOffset(engineCode);

        if (limit == 0 && offset > 0) {
            ctx.getBindings().add(offset);
            return "LIMIT -1 OFFSET " + parameterPlaceholder;
        }

        return super.compileLimit(ctx);
    }

    @Override
    protected String compileBasicDateCondition(SqlResult ctx, BasicDateCondition condition) {
        String column = wrap(condition.getColumn());
        String value = parameter(ctx, condition.getValue());

        Map<String, String> formatMap = new HashMap<>();
        formatMap.put("date", "%Y-%m-%d");
        formatMap.put("time", "%H:%M:%S");
        formatMap.put("year", "%Y");
        formatMap.put("month", "%m");
        formatMap.put("day", "%d");
        formatMap.put("hour", "%H");
        formatMap.put("minute", "%M");

        if (!formatMap.containsKey(condition.getPart())) {
            return column + " " + condition.getOperator() + " " + value;
        }

        String sql = String.format("strftime('%s', %s) %s cast(%s as text)",
                formatMap.get(condition.getPart()), column, condition.getOperator(), value);

        if (condition.isNot()) {
            return "NOT (" + sql + ")";
        }

        return sql;
    }


}
