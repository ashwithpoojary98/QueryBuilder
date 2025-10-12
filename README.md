# QueryBuilder

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ashwithpoojary98/querybuilder)
[![GitHub stars](https://img.shields.io/github/stars/ashwithpoojary98/querybuilder.svg?style=flat)](https://github.com/ashwithpoojary98/querybuilder/stargazers)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg?style=flat )](https://github.com/ashwithpoojary98/querybuilder/pulls)
[![GitHub forks](https://img.shields.io/github/forks/ashwithpoojary98/querybuilder.svg?style=social&label=Fork)](https://github.com/ashwithpoojary98/querybuilder/network)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight, fluent Java Query Builder inspired by .NET query builders like Dapper's SQL builder patterns and LINQ-style builders. This library provides an intuitive and type-safe way to construct SQL queries in Java applications.

## Maven Center Repository

https://mvnrepository.com/artifact/io.github.ashwithpoojary98/querybuilder

## Features

- Fluent API for building SQL queries
- Type-safe query construction
- Support for parameterized queries
- Easy integration with existing JDBC code
- Minimal dependencies
- Thread-safe

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.ashwithpoojary98</groupId>
    <artifactId>querybuilder</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.ashwithpoojary98:querybuilder:2.0.0'
```

## Usage

### Simple Select Query

```java
String query = QueryBuilder.select("name", "age")
    .from("users")
    .where("age > ?")
    .toString();
```

### Join Query

```java
String query = QueryBuilder.select("u.name", "o.order_date")
    .from("users u")
    .innerJoin("orders o").on("u.id = o.user_id")
    .where("o.status = ?")
    .toString();
```

### Insert Query

```java
String query = QueryBuilder.insertInto("users")
    .columns("name", "age", "email")
    .values("?, ?, ?")
    .toString();
```

### Update Query

```java
String query = QueryBuilder.update("users")
    .set("name = ?", "age = ?")
    .where("id = ?")
    .toString();
```

### Delete Query

```java
String query = QueryBuilder.deleteFrom("users")
    .where("inactive = true")
    .toString();
```

## Advanced Features

### Subqueries

```java
String subquery = QueryBuilder.select("id")
    .from("orders")
    .where("total > 1000")
    .toString();

String query = QueryBuilder.select("name")
    .from("users")
    .where("id IN (" + subquery + ")")
    .toString();
```

### Group By and Having

```java
String query = QueryBuilder.select("department", "COUNT(*) as employee_count")
    .from("employees")
    .groupBy("department")
    .having("COUNT(*) > ?")
    .toString();
```

## Query Builder Patterns

### Conditional Clauses

```java
public String buildDynamicQuery(String name, Integer age, String city) {
    QueryBuilder query = QueryBuilder.select("*").from("users");
    
    if (name != null) {
        query.where("name LIKE ?"); // Will be parameterized with: "%" + name + "%"
    }
    if (age != null) {
        query.where("age >= ?");
    }
    if (city != null) {
        query.where("city = ?");
    }
    
    return query.toString();
}
```

### Pagination

```java
public String buildPagedQuery(int page, int pageSize) {
    return QueryBuilder.select("*")
        .from("products")
        .orderBy("created_date DESC")
        .limit(pageSize)
        .offset((page - 1) * pageSize)
        .toString();
}
```

### Complex Joins

```java
String query = QueryBuilder.select("c.name", "o.order_id", "p.product_name", "s.status")
    .from("customers c")
    .innerJoin("orders o").on("c.id = o.customer_id")
    .leftJoin("products p").on("o.product_id = p.id")
    .leftJoin("shipments s").on("o.shipment_id = s.id")
    .where("o.order_date >= ?")
    .orderBy("o.order_date DESC")
    .toString();
```

## Error Handling Best Practices

### Connection Management

```java
public class DatabaseManager implements AutoCloseable {
    private final Connection connection;
    
    public DatabaseManager(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
        this.connection.setAutoCommit(false);
    }
    
    public void executeQuery(String query, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                // Process results
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error during rollback", ex);
            }
            throw new RuntimeException("Error executing query", e);
        }
    }
    
    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
```

## Performance Tips

1. **Use Prepared Statements**: Always use prepared statements with QueryBuilder to prevent SQL injection and improve performance through statement caching.

2. **Connection Pooling**: For production environments, use connection pooling:

```java
// HikariCP example
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
config.setUsername("user");
config.setPassword("password");
config.setMaximumPoolSize(10);

HikariDataSource dataSource = new HikariDataSource(config);

// Use with QueryBuilder
try (Connection conn = dataSource.getConnection()) {
    String query = QueryBuilder.select("*")
        .from("users")
        .where("status = ?")
        .toString();
    // Execute query
}
```

3. **Batch Operations**: For bulk inserts/updates:

```java
String query = QueryBuilder.insertInto("users")
    .columns("name", "email")
    .values("?, ?")
    .toString();

try (PreparedStatement stmt = conn.prepareStatement(query)) {
    for (User user : users) {
        stmt.setString(1, user.getName());
        stmt.setString(2, user.getEmail());
        stmt.addBatch();
    }
    stmt.executeBatch();
}
```

### Required Dependencies for Performance Features

```xml
<!-- HikariCP for connection pooling -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

## Database Connections

The QueryBuilder works seamlessly with various database connections. Here are examples for common databases:

### MySQL

```java
import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLExample {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/database_name";
        String username = "your_username";
        String password = "your_password";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String query = QueryBuilder.select("*")
                .from("users")
                .where("status = ?")
                .toString();
                
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, "active");
                ResultSet rs = stmt.executeQuery();
                // Process results
            }
        }
    }
}
```

### PostgreSQL

```java
public class PostgresExample {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/database_name";
        String username = "your_username";
        String password = "your_password";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String query = QueryBuilder.select("id", "email")
                .from("customers")
                .where("country = ?")
                .orderBy("email")
                .toString();
                
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, "USA");
                ResultSet rs = stmt.executeQuery();
                // Process results
            }
        }
    }
}
```

### Oracle

```java
public class OracleExample {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String username = "your_username";
        String password = "your_password";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String query = QueryBuilder.select("e.EMPLOYEE_ID", "e.FIRST_NAME", "d.DEPARTMENT_NAME")
                .from("EMPLOYEES e")
                .innerJoin("DEPARTMENTS d").on("e.DEPARTMENT_ID = d.DEPARTMENT_ID")
                .where("e.SALARY > ?")
                .toString();
                
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDouble(1, 50000.0);
                ResultSet rs = stmt.executeQuery();
                // Process results
            }
        }
    }
}
```

### SQL Server

```java
public class SQLServerExample {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=your_database";
        String username = "your_username";
        String password = "your_password";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String query = QueryBuilder.select("TOP 10 *")
                .from("Orders")
                .where("OrderDate >= ?")
                .orderBy("OrderDate DESC")
                .toString();
                
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDate(1, java.sql.Date.valueOf("2025-01-01"));
                ResultSet rs = stmt.executeQuery();
                // Process results
            }
        }
    }
}
```

### SQLite

```java
public class SQLiteExample {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:path_to_your_database.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            String query = QueryBuilder.select("*")
                .from("products")
                .where("price < ?")
                .limit(5)
                .toString();
                
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDouble(1, 100.0);
                ResultSet rs = stmt.executeQuery();
                // Process results
            }
        }
    }
}
```

### Required Dependencies

Add these dependencies to your `pom.xml` for the database you're using:

```xml
<!-- MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
</dependency>

<!-- Oracle -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>23.2.0.0</version>
</dependency>

<!-- SQL Server -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.1.jre11</version>
</dependency>

<!-- SQLite -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.43.0.0</version>
</dependency>
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## License

Copyright (c) 2025 QueryBuilder

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

