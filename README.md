# JMeter Xml2Sql

The project allows to parse a large JMeter xml result and insert all the rows in database. 


## Description

JMeter has a solution to create a report by Xslt processor.

But the solution is not perfect:
  - Xslt processor loads JMeter xml result in memory. Let's say if we have a large JMeter jtl file with a few millions of rows, we should run JVM with extra memory, otherwise OutOfMemory error will be occurred;
  - The report will be created for a current build only. We do not have a possibility to see a dynamic of loading tests result over period (days, months, years) to compare a result;
  - No possibility to create more advanced reports based on xml data.

The current project allows to parse a large JMeter jtl file and insert all the rows in database.
No extra memory consumed. Also the "batch" mode allows to insert a few millions rows into database very fast. 

As a result we can aggregate the data in database and prepare a report for a current build or a report for builds with predefined period.


## Building

### Pre-Building
The project works with MS SQL server now.

We should create a temp sql table to insert data from JMeter jtl file.

Check example in src\sql\tables\ti_LoadTestResult.sql and create the table in MS SQL server. 

### Compiling

```
$ mvn install
```

## Examples

#### Ant target to parse xml data and insert properties into database:


```
<!-- Create taskdef -->
<taskdef name="xml2sql" classname="com.sme.jmeter.xml.sax.SAXParser2SqlTask" classpathref="cp.all" />

<!--
Run task:
  - database properties: server, database, username, password
  - "srcFile" is JMeter xml file
  - "qualifiedName" is xml row to insert properties into database table
  - "table" is the sql table name
  - "columnsMap" allows to map xml to sql properties
  - "primaryColumns" is list of primary columns specified in sql table
  - "rowsToSql" specifies how many rows should be inserted while parsing xml data 
  - "inBatch" inserts rows in batch or step by step (recommended to use "batch" mode to insert rows fast)
  - "autoCommit" use sql connection with auto-commit option. This property works together with "inBatch" property
  - "cleanBeforeCreate" allows to clean up all data before inserting
 -->
<xml2sql server="${db_server}" database="${db_database}" username="${db_username}" password="${db_password}"
					srcFile="${basedir}/JMeterResults.jtl"
					qualifiedName="httpSample"
					table="ti_LoadTestResult"
					xmlColumnsMap="t:elapsedTime;it:idleTime;lt:latencyTime;ct:connectTime;ts:timeStamp;s:successFlag;lb:label;rc:responseCode;rm:responseMessage;tn:threadName;dt:dataType;by:bytes;sby:sentBytes;ng:activeThreadsInGroup;na:activeThreadsInAllGroups;hn:hostName;xt:correlationId;java.net.URL:request"
					primaryColumns="site=${primary.host};buildId=${build.id}"
					rowsToSql="10000"
					inBatch="true"
					autoCommit="false"
					cleanBeforeCreate="false" />

```

