package com.sme.jmeter.util;

import static org.apache.commons.dbcp.BasicDataSourceFactory.createDataSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Useful utilities to work with database operations.
 */
public class DbUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtil.class);
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    static
    {
        try
        {
            Class.forName(DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error loading SQLServerDriver driver");
        }
    }

    private DbUtil()
    {
    }

    /**
     * Creates data source instance.
     *
     * @param properties The database properties.
     * @return Returns {@link DataSource} from a config.
     */
    public static DataSource getDataSource(Properties properties)
    {
        properties.put("driverClassName", DRIVER);
        properties.put("url", getUrl(properties));

        try
        {
            DataSource dataSource = createDataSource(properties);

            LOGGER.info("Created data source: " + properties.getProperty("url"));
            return dataSource;
        }
        catch (Exception e)
        {
            String message = "Cannot create datasource";
            LOGGER.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * Creates data source instance.
     *
     * @param path The path with database properties.
     * @return Returns {@link DataSource} from a path.
     */
    public static DataSource getDataSource(String path)
    {
        Properties properties = loadProperties(path);
        return getDataSource(properties);
    }

    private static Properties loadProperties(String path)
    {
        try (InputStream stream = new FileInputStream(path))
        {
            Properties properties = new Properties();
            properties.load(stream);

            return properties;
        }
        catch (Exception e)
        {
            String message = "Cannot load properties";
            LOGGER.error(message, e);

            throw new IllegalArgumentException("Cannot load properties", e);
        }
    }

    /**
     * Creates url of database.
     * 
     * @param properties The database properties.
     * @return Returns url of database.
     */
    public static String getUrl(Properties properties)
    {
        String server = properties.getProperty("server");
        String database = StringUtils.defaultString(properties.getProperty("database"));

        StringBuilder url = new StringBuilder()
                .append("jdbc:sqlserver://")
                .append(server)
                .append(";databaseName=")
                .append(database);
        return url.toString();
    }
}
