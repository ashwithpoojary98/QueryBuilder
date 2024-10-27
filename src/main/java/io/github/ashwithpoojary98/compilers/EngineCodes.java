package io.github.ashwithpoojary98.compilers;

public enum EngineCodes {
    FIREBIRD("firebird"),
    GENERIC("generic"),
    MYSQL("mysql"),
    ORACLE("oracle"),
    POSTGRES("postgres"),
    SQLITE("sqlite"),
    SQL_SERVER("sqlsrv");

    private final String code;

    EngineCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

