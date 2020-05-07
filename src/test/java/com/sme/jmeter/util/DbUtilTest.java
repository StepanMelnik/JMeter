package com.sme.jmeter.util;

import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link DbUtil}.
 */
public class DbUtilTest extends Assertions
{
    @Test
    public void testDatasourceWithConfig() throws Exception
    {
        DataSource dataSource = DbUtil.getDataSource(DbUtilTest.class.getResource("/com/sme/jmeter/util/test_database.properties").getPath());

        assertNotNull(dataSource, "Expects created DataSource instance");
        assertNotNull(dataSource.getConnection(), " Expects created database connection");
    }

    @Test
    public void testDatasourceWithProperties() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty("server", "localhost:1433");
        properties.setProperty("database", "SpringTestCust");
        properties.setProperty("username", "spring");
        properties.setProperty("password", "test");

        DataSource dataSource = DbUtil.getDataSource(properties);

        assertNotNull(dataSource, "Expects created DataSource instance");
        assertNotNull(dataSource.getConnection(), " Expects created database connection");
    }
}
