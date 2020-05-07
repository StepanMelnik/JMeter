package com.sme.jmeter.sql;

import static com.sme.jmeter.util.DbUtil.getDataSource;
import static com.sme.jmeter.util.DbUtil.getUrl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ISqlRunner}.
 */
public class SqlRunner implements ISqlRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlRunner.class);

    private final QueryRunner queryRunner;
    private final String url;

    public SqlRunner(Properties properties)
    {
        queryRunner = new QueryRunner(getDataSource(properties));
        url = getUrl(properties);
    }

    @Override
    public int insert(String sql, Object... params)
    {
        try
        {
            logQuery("insert", () -> sql);
            return queryRunner.update(sql, params);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot insert data", e);
        }
    }

    @Override
    public int[] batch(boolean autoCommit, String sql, Object[][] params)
    {
        try (Connection connection = queryRunner.getDataSource().getConnection())
        {
            connection.setAutoCommit(autoCommit);

            logQuery("barch", () -> sql);
            int[] result = queryRunner.batch(connection, sql, params);

            if (!autoCommit)
            {
                connection.commit();
            }

            return result;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot insert data", e);
        }
    }

    @Override
    public int remove(String sql)
    {
        try
        {
            logQuery("update", () -> sql);
            return queryRunner.update(sql);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot remove data", e);
        }
    }

    @Override
    public List<Map<String, Object>> select(String sql)
    {
        try
        {
            logQuery("select", () -> sql);
            return queryRunner.query(sql, new MapListHandler());
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot select data", e);
        }
    }

    @Override
    public String getDatabaseUrl()
    {
        return url;
    }

    @Override
    public Map<String, Integer> getColumns(String table)
    {
        try
        {
            return queryRunner.query("select top 1 * from " + table, new MetaDataHandler());
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot fetch meta data from table: " + table, e);
        }
    }

    private void logQuery(String operation, Supplier<String> querySqlSupplier)
    {
        LOGGER.debug("Perfrom \"{}\" query in \"{}\" operation", querySqlSupplier.get(), operation);
    }
}
