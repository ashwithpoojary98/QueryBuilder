package io.github.ashwithpoojary98;

import io.github.ashwithpoojary98.exceptions.ParameterException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SqlResult {

    private String parameterPlaceholder;
    private String escapeCharacter;

    public SqlResult() {
    }

    public SqlResult(String parameterPlaceholder, String escapeCharacter) {
        this.parameterPlaceholder = parameterPlaceholder;
        this.escapeCharacter = escapeCharacter;
    }

    private Query query;
    private String rawSql = "";
    private List<Object> bindings = new ArrayList<>();
    private String sql = "";
    private Map<String, Object> namedBindings = new HashMap<>();

    private static final Class<?>[] NUMBER_TYPES = {
            Integer.class,
            Long.class,
            BigDecimal.class,
            Double.class,
            Float.class,
            Short.class,

    };

    @Override
    public String toString() {
        return toSql();
    }

    private String changeToSqlValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (Helper.isArray(value)) {
            return Helper.joinArray(",", (Iterable<?>) value);
        }

        for (Class<?> numberType : NUMBER_TYPES) {
            if (numberType.isInstance(value)) {
                return String.valueOf(value); // Adjust for localization if needed
            }
        }

        if (value instanceof Date) {
            SimpleDateFormat dateFormat;
            if (isDateOnly((Date) value)) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            } else {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            return "'" + dateFormat.format(value) + "'";
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        }

        if (value instanceof Enum) {
            return ((Enum<?>) value).ordinal() + " /* " + value + " */";
        }

        // Fallback to string
        return "'" + value.toString().replace("'", "''") + "'";
    }

    private boolean isDateOnly(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) == 0 &&
                cal.get(Calendar.MINUTE) == 0 &&
                cal.get(Calendar.SECOND) == 0 &&
                cal.get(Calendar.MILLISECOND) == 0;
    }

    // Getters and Setters (if needed)
    public String getParameterPlaceholder() {
        return parameterPlaceholder;
    }

    public void setParameterPlaceholder(String parameterPlaceholder) {
        this.parameterPlaceholder = parameterPlaceholder;
    }

    public String getEscapeCharacter() {
        return escapeCharacter;
    }

    public void setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return this.query;
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public String getRawSql() {
        return this.rawSql;
    }

    public void setBindings(List<Object> bindings) {
        this.bindings = bindings;
    }

    public List<Object> getBindings() {
        return this.bindings;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void setNamedBindings(Map<String, Object> namedBindings) {
        this.namedBindings = namedBindings;
    }

    public Map<String, Object> getNamedBindings() {
        return this.namedBindings;
    }

    public String toSql() {
        List<Object> deepParameters = Helper.flatten(bindings);

        String subject = Helper.replaceAll(rawSql, parameterPlaceholder, escapeCharacter, i -> {
            if (i >= deepParameters.size()) {
                throw new ParameterException(String.
                        format("Failed to retrieve a binding at index %d, the total bindings count is %s", i, bindings.size()));
            }

            Object value = deepParameters.get(i);
            return changeToSqlValue(value);
        });
        return Helper.removeEscapeCharacter(subject, parameterPlaceholder, escapeCharacter);
    }
}
