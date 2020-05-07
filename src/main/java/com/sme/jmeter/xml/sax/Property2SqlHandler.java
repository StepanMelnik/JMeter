package com.sme.jmeter.xml.sax;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.split;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sme.jmeter.sql.ISqlRunner;

/**
 * Sax parser to fetch xml data as properties and insert data into database.
 */
class Property2SqlHandler extends DefaultHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Property2SqlHandler.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final List<Map<String, String>> rows = new ArrayList<>();

    private final Deque<String> subElementStack = new ArrayDeque<>();
    private final Deque<StringBuilder> subElementTextStack = new ArrayDeque<>();
    private final Deque<Map<String, String>> rowStack = new ArrayDeque<>();

    private String qualifiedName;
    private int rowsToSql;
    private ISqlRunner sqlRunner;
    private String table;
    private String xmlColumnsMap;
    private String primaryColumns;

    private boolean inBatch;
    private boolean autoCommit;
    private boolean cleanBeforeCreate;

    private Map<String, Integer> columns;
    private Map<String, String> xml2SqlCollumnsMapping;
    private Map<String, String> primaryColumnsMap;
    private List<String> xmlAttributes;
    private String columnsList;
    private String columnsListValues;

    private long importedRows;

    Property2SqlHandler()
    {
    }

    @Override
    public void startDocument() throws SAXException
    {
        LOGGER.debug("Start to parse document. Work with database: " + sqlRunner.getDatabaseUrl());

        primaryColumnsMap = asList(split(primaryColumns, ";")).stream().collect(toMap(s -> split(s, "=")[0], s -> split(s, "=")[1]));

        if (cleanBeforeCreate)
        {
            LOGGER.debug("Remove data in database: " + sqlRunner.getDatabaseUrl());
            sqlRunner.remove(format("DELETE t FROM %s t", table));
        }

        columns = sqlRunner.getColumns(table);

        columnsList = columns.keySet().stream().map(s ->
        {
            return "[" + String.valueOf(s) + "]";
        }).collect(Collectors.joining(", "));

        columnsListValues = columns.keySet().stream().map(s ->
        {
            return "?";
        }).collect(Collectors.joining(", "));

        xml2SqlCollumnsMapping = asList(split(xmlColumnsMap, ";")).stream().collect(toMap(s -> split(s, ":")[1], s -> split(s, ":")[0]));
        xmlAttributes = asList(split(xmlColumnsMap, ";")).stream().map(s -> split(s, ":")[0]).collect(toList());
    }

    @Override
    public void startElement(String uri, String localName, String qname, Attributes attributes) throws SAXException
    {
        if (rows.size() == rowsToSql)
        {
            insert();
            rows.clear();
        }

        if (qname.equals(qualifiedName))
        {
            Map<String, String> row = new HashMap<>();

            for (int i = 0; i < attributes.getLength(); i++)
            {
                row.put(attributes.getQName(i), attributes.getValue(i));
            }

            rowStack.addLast(row);
        }

        // sub-element
        if (xmlAttributes.contains(qname))
        {
            subElementStack.addLast(qname);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qname) throws SAXException
    {
        if (!subElementStack.isEmpty())
        {
            rowStack.getLast().put(subElementStack.getLast(), subElementTextStack.getLast().toString());
        }

        if (qname.equals(qualifiedName))
        {
            rows.add(rowStack.getLast());
            rowStack.removeLast();
        }

        if (!subElementStack.isEmpty())
        {
            subElementTextStack.removeLast();
            subElementStack.removeLast();
        }
    }

    /**
     * ContentHandler may split text into several chunks to save memory.
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String content = new String(ch, start, length).trim();

        if (subElementTextStack.isEmpty())
        {
            subElementTextStack.addLast(new StringBuilder(content));
        }
        else
        {
            subElementTextStack.getLast().append(content);
        }
    }

    private void insert()
    {
        if (inBatch)
        {
            Object[][] params = new Object[rows.size()][columns.size()];

            // Old style, because common db-utils needs an array of objects in batch
            for (int i = 0; i < rows.size(); i++)
            {
                params[i] = createParams(rows.get(i));
                LOGGER.trace("Insert rows: " + Arrays.toString(params[i]));
            }

            String query = format("INSERT INTO %s (%s) VALUES (%s)", table, columnsList, columnsListValues);

            LOGGER.debug("Insert {} rows in batch into db", rows.size());
            sqlRunner.batch(autoCommit, query, params);
        }
        else
        {
            rows.forEach(row ->
            {
                Object[] params = createParams(row);
                LOGGER.trace("Insert rows: " + params);

                String query = format("INSERT INTO %s (%s) VALUES (%s)", table, columnsList, columnsListValues);
                LOGGER.debug("Insert {} rows into db", rows.size());
                sqlRunner.insert(query, params);
            });
        }

        importedRows = importedRows + rows.size();
        LOGGER.debug("Imported {} rows", importedRows);
    }

    /**
     * We cannot collect values by java8, because there is no guarantee that xml data is fetched in the same order as we use columns in database.
     * 
     * @param row The row of parsed xml data
     * @return Returns a list of objects.
     */
    private Object[] createParams(Map<String, String> row)
    {
        Object[] args = new Object[columns.size()];

        int i = -1;
        Iterator<String> iterator = columns.keySet().iterator();
        while (iterator.hasNext())
        {
            i++;

            String key = iterator.next();

            if (primaryColumnsMap.containsKey(key))
            {
                args[i] = primaryColumnsMap.get(key);
                continue;
            }

            Integer type = columns.get(key);

            String convertedKey = xml2SqlCollumnsMapping.get(key);
            String value = convertedKey == null ? null : row.get(convertedKey);

            Object arg = convertDatabaseValue(type, value);

            LOGGER.trace("key = " + key + "; value = " + arg);

            args[i] = arg;
        }
        return args;
    }

    private Object convertDatabaseValue(Integer type, String value)
    {
        switch (type)
        {
            case Types.SMALLINT:
                return BooleanUtils.toBoolean(value);

            case Types.TIMESTAMP:
                return SIMPLE_DATE_FORMAT.format(new Date(Long.valueOf(value)));

            default:
                return value;
        }
    }

    @Override
    public void endDocument() throws SAXException
    {
        insert();
        LOGGER.debug("Finish to parse document. Imported {} rows", importedRows);
    }

    /**
     * Builder class to avoid constructor with many arguments.
     */
    static final class Builder
    {
        private final Property2SqlHandler property2SqlHandler = new Property2SqlHandler();

        Builder qualifiedName(String qualifiedName)
        {
            property2SqlHandler.qualifiedName = qualifiedName;
            return this;
        }

        Builder rowsToSql(int rowsToSql)
        {
            property2SqlHandler.rowsToSql = rowsToSql;
            return this;
        }

        Builder table(String table)
        {
            property2SqlHandler.table = table;
            return this;
        }

        Builder xmlColumnsMap(String xmlColumnsMap)
        {
            property2SqlHandler.xmlColumnsMap = xmlColumnsMap;
            return this;
        }

        Builder primaryColumns(String primaryColumns)
        {
            property2SqlHandler.primaryColumns = primaryColumns;
            return this;
        }

        Builder inBatch(boolean inBatch)
        {
            property2SqlHandler.inBatch = inBatch;
            return this;
        }

        Builder autoCommit(boolean autoCommit)
        {
            property2SqlHandler.autoCommit = autoCommit;
            return this;
        }

        Builder cleanBeforeCreate(boolean cleanBeforeCreate)
        {
            property2SqlHandler.cleanBeforeCreate = cleanBeforeCreate;
            return this;
        }

        Builder sqlRunner(ISqlRunner sqlRunner)
        {
            property2SqlHandler.sqlRunner = sqlRunner;
            return this;
        }

        Property2SqlHandler build()
        {
            return property2SqlHandler;
        }
    }
}
