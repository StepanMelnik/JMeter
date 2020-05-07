package com.sme.jmeter.xml.sax;

import static com.sme.jmeter.assertion.JsonAssertion.toPrettyJson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sme.jmeter.assertion.JsonAssertion;
import com.sme.jmeter.util.ListBuilder;
import com.sme.jmeter.util.MapBuilder;

/**
 * Unit tests of {@link SAXParser2SqlTask}.
 */
public class SAXParser2SqlTaskTest extends Assertions
{
    @Test
    public void test_validate() throws Exception
    {
        SAXParser2SqlTask saxParser2Sql = new SAXParser2SqlTask();
        saxParser2Sql.setQualifiedName("httpSample");
        saxParser2Sql.setRowsToSql(50);

        assertThrows(NullPointerException.class, () -> saxParser2Sql.execute());
    }

    @Test
    public void test_insert() throws Exception
    {
        SAXParser2SqlTask saxParser2Sql = createTask(false);
        runAndAssert(saxParser2Sql);
    }

    @Test
    public void test_insert_in_batch() throws Exception
    {
        SAXParser2SqlTask saxParser2Sql = createTask(true);
        runAndAssert(saxParser2Sql);
    }

    private SAXParser2SqlTask createTask(boolean inBatch)
    {
        final String xmlColsMap = "t:elapsedTime;it:idleTime;lt:latencyTime;ct:connectTime;ts:timeStamp;s:successFlag;lb:label;rc:responseCode;rm:responseMessage;tn:threadName;dt:dataType;by:bytes;sby:sentBytes;ng:activeThreadsInGroup;na:activeThreadsInAllGroups;hn:hostName;xt:xTrack;java.net.URL:request";
        final String primaryColsMap = "site=www.sme.home.com;buildId=101";

        SAXParser2SqlTask saxParser2Sql = new SAXParser2SqlTask();
        saxParser2Sql.setSrcFile(SAXParser2SqlTaskTest.class.getResource("/com/sme/jmeter/xml/sax/JMeterResults.jtl").getPath());
        saxParser2Sql.setQualifiedName("httpSample");
        saxParser2Sql.setTable("ti_LoadTestResult");

        saxParser2Sql.setXmlColumnsMap(xmlColsMap);
        saxParser2Sql.setPrimaryColumns(primaryColsMap);
        saxParser2Sql.setInBatch(inBatch);
        saxParser2Sql.setCleanBeforeCreate(true);

        saxParser2Sql.setRowsToSql(2);

        saxParser2Sql.setServer("localhost:1433");
        saxParser2Sql.setDatabase("SpringTestCust");
        saxParser2Sql.setUsername("spring");
        saxParser2Sql.setPassword("test");
        return saxParser2Sql;
    }

    private void runAndAssert(SAXParser2SqlTask saxParser2Sql)
    {
        saxParser2Sql.execute();

        List<Map<String, Object>> data = saxParser2Sql.getSqlRunner()
                .select("SELECT site, buildId, elapsedTime, idleTime, latencyTime, connectTime, timeStamp, successFlag, label, responseCode, responseMessage, "
                    + "threadName, dataType, bytes, sentBytes, activeThreadsInGroup, activeThreadsInAllGroups, hostName, xTrack, request "
                    + "FROM ti_LoadTestResult WITH (NOLOCK) ORDER BY id");

        List<Map<String, Object>> actualData = data.stream().map(r -> new LinkedHashMap<>(r)).collect(Collectors.toList());

        List<Map<String, Object>> expectedData = new ListBuilder<Map<String, Object>>()
                .add(new MapBuilder<String, Object>(true)
                        .put("site", "www.sme.home.com")
                        .put("buildId", 101)
                        .put("elapsedTime", 65)
                        .put("idleTime", 0)
                        .put("latencyTime", 65)
                        .put("connectTime", 51)
                        .put("timeStamp", 1553856693000L)
                        .put("successFlag", 1)
                        .put("label", "First page")
                        .put("responseCode", "200")
                        .put("responseMessage", "OK")
                        .put("threadName", "Ultimate Thread Group, Main 1-8")
                        .put("dataType", "text")
                        .put("bytes", 2268)
                        .put("sentBytes", 447)
                        .put("activeThreadsInGroup", 4)
                        .put("activeThreadsInAllGroups", 4)
                        .put("hostName", "StepanHome1")
                        .put("xTrack", "2019-11-19T03:44:45")
                        .put("request", "https://www.sme.home.com/1")
                        .build())
                .add(new MapBuilder<String, Object>(true)
                        .put("site", "www.sme.home.com")
                        .put("buildId", 101)
                        .put("elapsedTime", 77)
                        .put("idleTime", 0)
                        .put("latencyTime", 76)
                        .put("connectTime", 55)
                        .put("timeStamp", 1553856748000L)
                        .put("successFlag", 1)
                        .put("label", "First page")
                        .put("responseCode", "200")
                        .put("responseMessage", "OK")
                        .put("threadName", "Ultimate Thread Group, Main 1-9")
                        .put("dataType", "text")
                        .put("bytes", 2268)
                        .put("sentBytes", 447)
                        .put("activeThreadsInGroup", 5)
                        .put("activeThreadsInAllGroups", 5)
                        .put("hostName", "StepanHome2")
                        .put("xTrack", "2019-11-19T03:45:45")
                        .put("request", "https://www.sme.home.com/2")
                        .build())
                .add(new MapBuilder<String, Object>(true)
                        .put("site", "www.sme.home.com")
                        .put("buildId", 101)
                        .put("elapsedTime", 67)
                        .put("idleTime", 0)
                        .put("latencyTime", 67)
                        .put("connectTime", 58)
                        .put("timeStamp", 1553856748000L)
                        .put("successFlag", 1)
                        .put("label", "First page")
                        .put("responseCode", "200")
                        .put("responseMessage", "OK")
                        .put("threadName", "Ultimate Thread Group, Main 1-10")
                        .put("dataType", "text")
                        .put("bytes", 2268)
                        .put("sentBytes", 447)
                        .put("activeThreadsInGroup", 5)
                        .put("activeThreadsInAllGroups", 5)
                        .put("hostName", "StepanHome2")
                        .put("xTrack", "2019-11-19T03:45:59")
                        .put("request", "https://www.sme.home.com/3")
                        .build())
                .add(new MapBuilder<String, Object>(true)
                        .put("site", "www.sme.home.com")
                        .put("buildId", 101)
                        .put("elapsedTime", 61)
                        .put("idleTime", 0)
                        .put("latencyTime", 61)
                        .put("connectTime", 51)
                        .put("timeStamp", 1553856748000L)
                        .put("successFlag", 1)
                        .put("label", "First page")
                        .put("responseCode", "200")
                        .put("responseMessage", "OK")
                        .put("threadName", "Ultimate Thread Group, Main 1-10")
                        .put("dataType", "text")
                        .put("bytes", 2268)
                        .put("sentBytes", 447)
                        .put("activeThreadsInGroup", 5)
                        .put("activeThreadsInAllGroups", 5)
                        .put("hostName", "StepanHome1")
                        .put("xTrack", "2019-11-19T03:46:45")
                        .put("request", "https://www.sme.home.com/4")
                        .build())
                .add(new MapBuilder<String, Object>(true)
                        .put("site", "www.sme.home.com")
                        .put("buildId", 101)
                        .put("elapsedTime", 66)
                        .put("idleTime", 0)
                        .put("latencyTime", 65)
                        .put("connectTime", 52)
                        .put("timeStamp", 1553856748000L)
                        .put("successFlag", 1)
                        .put("label", "First page")
                        .put("responseCode", "200")
                        .put("responseMessage", "OK")
                        .put("threadName", "Ultimate Thread Group, Main 1-10")
                        .put("dataType", "text")
                        .put("bytes", 2268)
                        .put("sentBytes", 447)
                        .put("activeThreadsInGroup", 4)
                        .put("activeThreadsInAllGroups", 4)
                        .put("hostName", "StepanHome1")
                        .put("xTrack", "2019-11-19T03:46:50")
                        .put("request", "https://www.sme.home.com/5")
                        .build())
                .build();

        assertEquals(JsonAssertion.toPrettyJson(expectedData), toPrettyJson(actualData));
    }

    @Disabled
    @Test
    public void test_large_file_parser() throws Exception
    {
        final String xmlColsMap = "t:elapsedTime;it:idleTime;lt:latencyTime;ct:connectTime;ts:timeStamp;s:successFlag;lb:label;rc:responseCode;rm:responseMessage;tn:threadName;dt:dataType;by:bytes;sby:sentBytes;ng:activeThreadsInGroup;na:activeThreadsInAllGroups;hn:hostName;xt:xTrack;java.net.URL:request";
        final String primaryColsMap = "site=www.sme.home.com;buildId=110";

        SAXParser2SqlTask saxParser2Sql = new SAXParser2SqlTask();
        saxParser2Sql.setSrcFile("/jmeter/Large_JMeterResults.jtl");
        saxParser2Sql.setQualifiedName("httpSample");
        saxParser2Sql.setTable("LoadTestResult");
        saxParser2Sql.setXmlColumnsMap(xmlColsMap);
        saxParser2Sql.setPrimaryColumns(primaryColsMap);
        saxParser2Sql.setInBatch(true);
        saxParser2Sql.setAutoCommit(false);
        saxParser2Sql.setCleanBeforeCreate(true);

        saxParser2Sql.setRowsToSql(5000);

        saxParser2Sql.setServer("localhost:1433");
        saxParser2Sql.setDatabase("SpringTestCust");
        saxParser2Sql.setUsername("sprint");
        saxParser2Sql.setPassword("test");

        saxParser2Sql.execute();
    }
}
