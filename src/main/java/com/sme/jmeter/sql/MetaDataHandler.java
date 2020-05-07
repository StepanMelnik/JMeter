package com.sme.jmeter.sql;

import static java.util.Arrays.asList;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Sql meta data handler to fetch columns except a list of ignored columns.
 */
public class MetaDataHandler implements ResultSetHandler<Map<String, Integer>>
{
    private static final List<String> IGNORE_COLUMNS = asList("id");

    @Override
    public Map<String, Integer> handle(ResultSet rs) throws SQLException
    {
        Map<String, Integer> map = new LinkedHashMap<>();

        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        for (int column = 1; column <= cols; column++)
        {
            String columnName = meta.getColumnName(column);
            if (!IGNORE_COLUMNS.contains(columnName))
            {
                int columnType = meta.getColumnType(column);
                map.put(columnName, columnType);
            }
        }

        return map;
    }
}
