package io.github.ashwithpoojary98.compilers;

import io.github.ashwithpoojary98.SqlResult;
import io.github.ashwithpoojary98.UnsafeLiteral;
import io.github.ashwithpoojary98.clausses.conditions.BasicDateCondition;
import io.github.ashwithpoojary98.clausses.conditions.BasicStringCondition;

import java.util.Arrays;

public class PostgresCompiler extends Compiler {

    public PostgresCompiler() {
        lastId = "SELECT lastval() AS id";
        this.engineCode = EngineCodes.POSTGRES.getCode();
    }

    public String getEngineCode() {
        return this.engineCode;
    }

    public void setEngineCode() {
        this.engineCode = EngineCodes.POSTGRES.getCode();
    }

    protected String compileBasicStringCondition(SqlResult ctx, BasicStringCondition x) {
        String column = wrap(x.getColumn());

        String value = (String) resolve(ctx, x.getValue());

        if (value == null) {
            throw new IllegalArgumentException("Expecting a non-nullable string");
        }

        String method = x.getOperator();

        if (Arrays.asList("starts", "ends", "contains", "like", "ilike").contains(x.getOperator())) {
            method = x.isCaseSensitive() ? "LIKE" : "ILIKE";

            switch (x.getOperator()) {
                case "starts":
                    value = value + "%";
                    break;
                case "ends":
                    value = "%" + value;
                    break;
                case "contains":
                    value = "%" + value + "%";
                    break;
            }
        }

        String sql;

        if (x.getValue() instanceof UnsafeLiteral) {
            sql = column + " " + checkOperator(method) + " " + value;
        } else {
            sql = column + " " + checkOperator(method) + " " + parameter(ctx, value);
        }

        String currentEscapeCharacter = x.getEscapeCharacter();
        if (currentEscapeCharacter != null && !currentEscapeCharacter.isEmpty()) {
            sql += " ESCAPE '" + currentEscapeCharacter + "'";
        }

        return x.isNot() ? "NOT (" + sql + ")" : sql;
    }

    protected String compileBasicDateCondition(SqlResult ctx, BasicDateCondition condition) {
        String column = wrap(condition.getColumn());

        String left;

        if ("time".equals(condition.getPart())) {
            left = column + "::time";
        } else if ("date".equals(condition.getPart())) {
            left = column + "::date";
        } else {
            left = "DATE_PART('" + condition.getPart().toUpperCase() + "', " + column + ")";
        }

        String sql = left + " " + condition.getOperator() + " " + parameter(ctx, condition.getValue());

        return condition.isNot() ? "NOT (" + sql + ")" : sql;
    }

}
