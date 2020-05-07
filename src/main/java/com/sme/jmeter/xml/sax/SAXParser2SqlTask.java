package com.sme.jmeter.xml.sax;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.SAXException;

import com.sme.jmeter.sql.ISqlRunner;
import com.sme.jmeter.sql.SqlRunner;

/**
 * Ant target to parse xml data and insert properties into database.
 */
public class SAXParser2SqlTask extends Task
{
    private static final int ROWS_TO_SQL = 100;

    private int rowsToSql = ROWS_TO_SQL;
    private String qualifiedName;
    private String table;
    private String xmlColumnsMap = "";
    private String primaryColumns = "";
    private String srcFile;
    private boolean inBatch;
    private boolean autoCommit;
    private boolean cleanBeforeCreate;

    private String server;
    private String database;
    private String username;
    private String password;

    private ISqlRunner sqlRunner;

    @Override
    public void execute() throws BuildException
    {
        validate();

        sqlRunner = createSqlRunner();
        Property2SqlHandler handler = new Property2SqlHandler.Builder()
                .qualifiedName(qualifiedName)
                .rowsToSql(rowsToSql)
                .table(table)
                .xmlColumnsMap(xmlColumnsMap)
                .primaryColumns(primaryColumns)
                .inBatch(inBatch)
                .autoCommit(autoCommit)
                .cleanBeforeCreate(cleanBeforeCreate)
                .sqlRunner(sqlRunner)
                .build();

        File inputFile = new File(srcFile);
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try
        {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputFile, handler);
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            throw new BuildException("Cannot parse file: " + srcFile, e);
        }
    }

    private ISqlRunner createSqlRunner()
    {
        Properties properties = new Properties();
        properties.setProperty("server", server);
        properties.setProperty("database", database);
        properties.setProperty("username", username);
        properties.setProperty("password", password);

        return new SqlRunner(properties);
    }

    private void validate()
    {
        requireNonNull(qualifiedName, "Requires 'qualifiedName' parameter");
        requireNonNull(table, "Requires 'table' parameter");
        requireNonNull(srcFile, "Requires 'srcFile' parameter");

        requireNonNull(server, "Requires 'server' parameter");
        requireNonNull(database, "Requires 'database' parameter");
        requireNonNull(username, "Requires 'username' parameter");
        requireNonNull(password, "Requires 'password' parameter");
    }

    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }

    public void setTable(String table)
    {
        this.table = table;
    }

    public void setXmlColumnsMap(String xmlColumnsMap)
    {
        this.xmlColumnsMap = xmlColumnsMap;
    }

    public void setPrimaryColumns(String primaryColumns)
    {
        this.primaryColumns = primaryColumns;
    }

    public void setRowsToSql(int rowsToSql)
    {
        this.rowsToSql = rowsToSql;
    }

    public void setSrcFile(String srcFile)
    {
        this.srcFile = srcFile;
    }

    public void setInBatch(boolean inBatch)
    {
        this.inBatch = inBatch;
    }

    public void setAutoCommit(boolean autoCommit)
    {
        this.autoCommit = autoCommit;
    }

    public void setCleanBeforeCreate(boolean cleanBeforeCreate)
    {
        this.cleanBeforeCreate = cleanBeforeCreate;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Prepares sql runner for test purpose.
     * 
     * @VisibleForTesting
     * @return Returns sql runner to perform extra sql data.
     */
    ISqlRunner getSqlRunner()
    {
        return sqlRunner;
    }
}
