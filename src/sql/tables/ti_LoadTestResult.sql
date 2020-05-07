--------------------------------------------------------------------
-- ti_LoadTestResult table.
-- Temp table to create request-response data of loading tests.
--------------------------------------------------------------------

	-- DROP TABLE ti_LoadTestResult
	IF (OBJECT_ID('ti_LoadTestResult.sql', N'U') IS NULL)
	BEGIN
		CREATE TABLE dbo.ti_LoadTestResult
		(
			id				INT		IDENTITY NOT NULL		-- Ant task ignores IDENTITY column with id name
		,	site				VARCHAR(100)	NOT NULL DEFAULT('')		-- Site name that all requests are connected to
		,	buildId				INT		NOT NULL DEFAULT(0)		-- The id of jenkins build that creates xml to sql data
		,	elapsedTime			INT		NOT NULL DEFAULT(0)		-- Elapsed time in milliseconds	
		,	idleTime			INT		NOT NULL DEFAULT(0)		-- Idle Time = time not spent sampling in milliseconds (generally 0)
		,	latencyTime			INT		NOT NULL DEFAULT(0)		-- Latency is time to initial response in milliseconds (not all samplers support this)
		,	connectTime			INT		NOT NULL DEFAULT(0)		-- Connect Time is time to establish the connection in milliseconds (not all samplers support this)
		,	timeStamp			DATETIME	NOT NULL DEFAULT(GETDATE())	-- timeStamp in milliseconds (since midnight Jan 1, 1970 UTC)
		,	successFlag			SMALLINT	NOT NULL DEFAULT(0)		-- Success flag (true/false)
		,	label				NVARCHAR(2000)	NOT NULL DEFAULT('')		-- Label or path of request
		,	responseCode			VARCHAR(4000)	NOT NULL DEFAULT('')		-- Response code. For example, 200. If error occurs, could be error message
		,	responseMessage			VARCHAR(4000)	NOT NULL DEFAULT('')		-- Response message. For example, OK. If error occurs, could be error message
		,	threadName			VARCHAR(100)	NOT NULL DEFAULT('')		-- Thread name
		,	dataType			VARCHAR(10)	NOT NULL DEFAULT('')		-- Data type. For example, text
		,	bytes				INT		NOT NULL DEFAULT(0)		-- Bytes of response
		,	sentBytes			INT		NOT NULL DEFAULT(0)		-- Sent bytes
		,	activeThreadsInGroup		INT		NOT NULL DEFAULT(0)		-- Number of active threads in this group
		,	activeThreadsInAllGroups	INT		NOT NULL DEFAULT(0)		-- Number of active threads for all thread groups
		,	hostName			VARCHAR(100)	NOT NULL DEFAULT('')		-- Hostname where the sample was generated
		,	request				NVARCHAR(2000)	NULL DEFAULT('''')		-- Url of called request
		,	xTrack				NVARCHAR(1000)	NULL DEFAULT('''')		-- Additional info to track a request. For example, use timestamp in the request header when a request is created
		)
	END

--------------------------------------------------------------------
-- New/Changed columns
--------------------------------------------------------------------


--------------------------------------------------------------------
-- Indices
--------------------------------------------------------------------

	-- Add the clustered index
	IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = N'CIX_LoadTestResult')
		CREATE CLUSTERED INDEX CIX_ArticleID ON ti_LoadTestResult(id, site, buildId)

	-- Unique Indexes (For lookups when generating report)
	IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = N'UI_LoadTestResult')
		CREATE UNIQUE INDEX UI_Article_Name ON ti_LoadTestResult(timeStamp, buildId, site, label, id)

--------------------------------------------------------------------
-- Foreign keys
--------------------------------------------------------------------

--------------------------------------------------------------------
-- Change Tracking
--------------------------------------------------------------------

--------------------------------------------------------------------
-- Constraints
--------------------------------------------------------------------

--------------------------------------------------------------------
-- ti_LoadTestResult table end
--------------------------------------------------------------------

GO
