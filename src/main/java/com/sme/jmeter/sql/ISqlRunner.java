package com.sme.jmeter.sql;

import java.util.List;
import java.util.Map;

/**
 * Sql runner.
 */
public interface ISqlRunner
{
    /**
     * Executes the given INSERT SQL statement.
     *
     * @param sql The SQL statement to execute
     * @param params Initializes the PreparedStatement's IN (i.e. '?') parameters
     * @return The number of rows updated.
     */
    int insert(String sql, Object... params);

    /**
     * Executes the given INSERT SQL statement in batch.
     * 
     * @param autoCommit To use sql connection with auto-commit option
     * @param sql The SQL statement to execute
     * @param params Initializes the PreparedStatement's IN (i.e. '?') parameters
     * @return The number of rows updated.
     */
    int[] batch(boolean autoCommit, String sql, Object[][] params);

    /**
     * Executes the given DELETE SQL statement.
     *
     * @param sql The SQL statement to execute
     * @return The number of rows removed.
     */
    int remove(String sql);

    /**
     * Fetches data from database.
     * 
     * @param sql The SQL statement to execute
     * @return Returns a list of rows as map.
     */
    List<Map<String, Object>> select(String sql);

    /**
     * Creates url of database.
     * 
     * @return Returns url of database.
     */
    String getDatabaseUrl();

    /**
     * Creates columns metadata of the given table.
     * 
     * @param table The table to fetch metadata
     * @return Returns metadata of columns.
     */
    Map<String, Integer> getColumns(String table);
}
