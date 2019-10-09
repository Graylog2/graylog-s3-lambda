package org.graylog.integrations.s3;

import com.github.joschi.jadconfig.Parameter;

/**
 * Reads configuration values from environment variables defined on the Lambda function.
 *
 * Defaults are specified in the getter methods.
 *
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/tutorial-env_cli.html">Lambda Environment Variables</a>
 */
public class Configuration {

    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_RECONNECT_DELAY = 10000;
    private static final int DEFAULT_TCP_QUEUE_SIZE = 512;
    private static final int DEFAULT_TCP_MAX_IN_FLIGHT_SENDS = 512;
    private static final int DEFAULT_SHUTDOWN_FLUSH_TIMEOUT_MS = 100;
    private static final int DEFAULT_SHUTDOWN_FLUSH_RETRIES = 6000;
    private static final String DEFAULT_MESSAGE_SUMMARY_FIELDS = "ClientRequestHost,ClientRequestPath,OriginIP,ClientSrcPort,EdgeServerIP,EdgeResponseBytes";

    // Environment variables with these names can be defined on the Lambda function to specify values.
    private static final String S3_BUCKET_NAME = "S3_BUCKET_NAME";
    private static final String GRAYLOG_HOST = "GRAYLOG_HOST";
    private static final String GRAYLOG_PORT = "GRAYLOG_PORT";
    private static final String CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    private static final String RECONNECT_DELAY = "RECONNECT_DELAY";
    private static final String TCP_KEEP_ALIVE = "TCP_KEEP_ALIVE";
    private static final String TCP_NO_DELAY = "TCP_NO_DELAY";
    private static final String TCP_QUEUE_SIZE = "TCP_QUEUE_SIZE";
    private static final String TCP_MAX_IN_FLIGHT_SENDS = "TCP_MAX_IN_FLIGHT_SENDS";
    private static final String CONTENT_TYPE = "CONTENT_TYPE";
    private static final String COMPRESSION_TYPE = "COMPRESSION_TYPE";
    private static final String PROTOCOL_TYPE = "PROTOCOL_TYPE";
    private static final String SHUTDOWN_FLUSH_TIMEOUT_MS = "SHUTDOWN_FLUSH_TIMEOUT_MS";
    private static final String SHUTDOWN_FLUSH_RETRIES = "SHUTDOWN_FLUSH_RETRIES";

    // Logpush config
    private static final String LOG_PUSH_PREFIX = "CLOUDFLARE_LOGPUSH_";

    private static final String LOGPUSH_USE_NOW_TIMESTAMP = LOG_PUSH_PREFIX + "USE_NOW_TIMESTAMP";
    private static final String LOGPUSH_MESSAGE_FIELDS = LOG_PUSH_PREFIX + "MESSAGE_FIELDS";
    // Fields to store in the message field in the GELF message field.
    private static final String LOGPUSH_MESSAGE_SUMMARY_FIELDS = LOG_PUSH_PREFIX + "MESSAGE_SUMMARY_FIELDS";

    @Parameter(value = S3_BUCKET_NAME, required = true)
    private String s3BucketName;

    @Parameter(value = GRAYLOG_HOST, required = true)
    private String graylogHost;

    @Parameter(value = GRAYLOG_PORT, required = true)
    private Integer graylogPort;

    @Parameter(CONNECT_TIMEOUT)
    private Integer connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    @Parameter(RECONNECT_DELAY)
    private Integer reconnectDelay = DEFAULT_RECONNECT_DELAY;

    @Parameter(TCP_KEEP_ALIVE)
    private Boolean tcpKeepAlive = true;

    @Parameter(TCP_NO_DELAY)
    private Boolean tcpNoDelay = true;

    @Parameter(TCP_QUEUE_SIZE)
    private Integer queueSize = DEFAULT_TCP_QUEUE_SIZE;

    @Parameter(TCP_MAX_IN_FLIGHT_SENDS)
    private Integer maxInflightSends = DEFAULT_TCP_MAX_IN_FLIGHT_SENDS;

    @Parameter(value = CONTENT_TYPE, required = true)
    private String contentType;

    @Parameter(COMPRESSION_TYPE)
    private String compressionType;

    @Parameter(PROTOCOL_TYPE)
    private String protocolType;

    // The number of milliseconds to wait for messages to finish sending during shutdown.
    @Parameter(SHUTDOWN_FLUSH_TIMEOUT_MS)
    private Integer shutdownFlushTimeoutMs = DEFAULT_SHUTDOWN_FLUSH_TIMEOUT_MS;

    // How many times to retry the shutdownFlushTimeoutMs wait while waiting for messages to
    // finish sending during shutdown.
    @Parameter(SHUTDOWN_FLUSH_RETRIES)
    private Integer shutdownFlushReties = DEFAULT_SHUTDOWN_FLUSH_RETRIES;

    // Overrides message timestamp with the current time.
    @Parameter(LOGPUSH_USE_NOW_TIMESTAMP)
    private Boolean useNowTimestamp = false;

    // Fields to parse and store with the message in Graylog
    @Parameter(LOGPUSH_MESSAGE_FIELDS)
    private String messageFields;

    @Parameter(LOGPUSH_MESSAGE_SUMMARY_FIELDS)
    private String messageSummaryFields = DEFAULT_MESSAGE_SUMMARY_FIELDS;

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getGraylogHost() {
        return graylogHost;
    }

    public Integer getGraylogPort() {
        return graylogPort;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getReconnectDelay() {
        return reconnectDelay;
    }

    public Boolean getTcpKeepAlive() {
        return tcpKeepAlive;
    }

    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public Integer getMaxInflightSends() {
        return maxInflightSends;
    }

    public ContentType getContentType() {
        return ContentType.findByType(contentType);
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType.getType();
    }

    public CompressionType getCompressionType() {
        return CompressionType.findByType(compressionType);
    }

    public ProtocolType getProtocolType() {
        return ProtocolType.findByType(protocolType);
    }

    public Integer getShutdownFlushTimeoutMs() {
        return shutdownFlushTimeoutMs;
    }

    public Integer getShutdownFlushReties() {
        return shutdownFlushReties;
    }

    public Boolean getUseNowTimestamp() {
        return useNowTimestamp;
    }

    public String getMessageFields() {
        return messageFields;
    }

    public void setMessageFields(String messageFields) {
        this.messageFields = messageFields;
    }

    public String getMessageSummaryFields() {
        return messageSummaryFields;
    }

    public void setMessageSummaryFields(String messageSummaryFields) {
        this.messageSummaryFields = messageSummaryFields;
    }

    @Override
    public String toString() {
        return "Configuration{" +
               "s3BucketName='" + s3BucketName + '\'' +
               ", graylogHost='" + graylogHost + '\'' +
               ", graylogPort=" + graylogPort +
               ", connectTimeout=" + connectTimeout +
               ", reconnectDelay=" + reconnectDelay +
               ", tcpKeepAlive=" + tcpKeepAlive +
               ", tcpNoDelay=" + tcpNoDelay +
               ", queueSize=" + queueSize +
               ", maxInflightSends=" + maxInflightSends +
               ", contentType='" + contentType + '\'' +
               ", compressionType='" + compressionType + '\'' +
               ", protocolType='" + protocolType + '\'' +
               ", useNowTimestamp=" + useNowTimestamp +
               ", messageFields='" + messageFields + '\'' +
               ", messageSummaryFields='" + messageSummaryFields + '\'' +
               '}';
    }
}
