package org.graylog.integrations.s3;

/**
 * This class reads the needed configuration values from environment variables defined on the S3 function.
 *
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/tutorial-env_cli.html">S3 Environment Variables</a>
 */
public class Config {

    private static final String DEFAULT_MESSAGE_SUMMARY_FIELDS = "ClientRequestHost,ClientRequestPath,OriginIP,ClientSrcPort,EdgeServerIP,EdgeResponseBytes";
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    // Use newInstance() instead.
    private Config() {
    }

    // Each of these environment variables need to be defined on the Lambda function.
    private static final String S3_BUCKET_NAME = "s3_bucket_name";
    private static final String GRAYLOG_HOST = "graylog_host";
    private static final String GRAYLOG_PORT = "graylog_port";
    private static final String CONNECT_TIMEOUT = "connect_timeout";
    private static final String RECONNECT_DELAY = "reconnect_delay";
    private static final String TCP_KEEP_ALIVE = "tcp_keep_alive";
    private static final String TCP_NO_DELAY = "tcp_no_delay";
    private static final String TCP_QUEUE_SIZE = "tcp_queue_size";
    private static final String TCP_MAX_IN_FLIGHT_SENDS = "tcp_max_in_flight_sends";
    private static final String USE_NOW_TIMESTAMP = "use_now_timestamp";
    // Fields to parse and store with the message in Graylog
    private static final String MESSAGE_FIELDS = "message_fields";
    // Fields to store in the message field in the GELF message field.
    private static final String MESSAGE_SUMMARY_FIELDS = "message_summary_fields";
    private static final String CONTENT_TYPE = "content_type";

    private String s3BucketName;

    // Transport settings
    private String graylogHost;
    private Integer graylogPort;
    private Integer connectTimeout;
    private Integer reconnectDelay;
    private Boolean tcpKeepAlive;
    private Boolean tcpNoDelay;
    private Integer queueSize;
    private Integer maxInflightSends;
    private Boolean useNowTimestamp;

    // Defaults to all fields
    private String messageFields;

    // Defaults to the indicated fields
    private String messageSummaryFields;

    private ContentType contentType;


    public static Config newInstance() {

        final Config config = new Config();
        config.s3BucketName = System.getenv(S3_BUCKET_NAME);
        config.graylogHost = System.getenv(GRAYLOG_HOST);
        config.graylogPort = safeParseInteger(GRAYLOG_PORT);
        config.connectTimeout = safeParseInteger(CONNECT_TIMEOUT) != null ? safeParseInteger(CONNECT_TIMEOUT) : 10000;
        config.reconnectDelay = safeParseInteger(RECONNECT_DELAY) != null ? safeParseInteger(RECONNECT_DELAY) : 10000;
        config.tcpKeepAlive = readBoolean(TCP_KEEP_ALIVE, true);
        config.tcpNoDelay = readBoolean(TCP_NO_DELAY, true);
        config.queueSize = safeParseInteger(TCP_QUEUE_SIZE) != null ? safeParseInteger(TCP_QUEUE_SIZE) : 512;

        // Max inflight sends must be 1 in order for synchronous sending VIA gelfclient to work.
        // This forces the client to send the messages serially. Once the queue size is zero, then the transport can be shut down.
        config.maxInflightSends = safeParseInteger(TCP_MAX_IN_FLIGHT_SENDS) != null ? safeParseInteger(TCP_MAX_IN_FLIGHT_SENDS) : 512;

        config.useNowTimestamp = readBoolean(USE_NOW_TIMESTAMP, false);

        config.messageFields = getStringEnvironmentVariable(MESSAGE_FIELDS, null);
        config.messageSummaryFields = getStringEnvironmentVariable(MESSAGE_SUMMARY_FIELDS, DEFAULT_MESSAGE_SUMMARY_FIELDS);

        config.contentType = ContentType.findByType(getStringEnvironmentVariable(CONTENT_TYPE, DEFAULT_CONTENT_TYPE));

        return config;
    }

    private static boolean readBoolean(String property, boolean defaultValue) {
        return System.getenv(property) != null ? Boolean.valueOf(System.getenv(property)) : defaultValue;
    }

    /**
     * @return Get the indicated string environment variable or return the default value if not present.
     */
    private static String getStringEnvironmentVariable(String envVarName, String defaultValue) {
        return System.getenv(envVarName) != null && !System.getenv(envVarName).trim().isEmpty() ? System.getenv(envVarName) : defaultValue;
    }

    /**
     * Read the specified environment variable and attempt to convert it to an integer. Handle error condition.
     *
     * @param envVariableName The environment variable name to parse.
     * @return The parsed integer.
     */
    private static Integer safeParseInteger(String envVariableName) {

        final String envValue = System.getenv(envVariableName);
        // Safely ignore blank values.
        if (envValue == null || envValue.equals(envVariableName)) {
            return null;
        }
        try {
            return Integer.valueOf(envValue);
        } catch (NumberFormatException e) {
            final String errorMessage = String.format("The specified value [%s] for field [%s] is not a valid integer.",
                                                      envValue, envVariableName);
            e.printStackTrace();
            throw new RuntimeException(errorMessage);
        }
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getGraylogHost() {
        return graylogHost;
    }

    public void setGraylogHost(String graylogHost) {
        this.graylogHost = graylogHost;
    }

    public Integer getGraylogPort() {
        return graylogPort;
    }

    public void setGraylogPort(Integer graylogPort) {
        this.graylogPort = graylogPort;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(Integer reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public Boolean getTcpKeepAlive() {
        return tcpKeepAlive;
    }

    public void setTcpKeepAlive(Boolean tcpKeepAlive) {
        this.tcpKeepAlive = tcpKeepAlive;
    }

    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getMaxInflightSends() {
        return maxInflightSends;
    }

    public void setMaxInflightSends(Integer maxInflightSends) {
        this.maxInflightSends = maxInflightSends;
    }

    public Boolean getUseNowTimestamp() {
        return useNowTimestamp;
    }

    public void setUseNowTimestamp(Boolean useNowTimestamp) {
        this.useNowTimestamp = useNowTimestamp;
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

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "Config{" +
               "s3BucketName='" + s3BucketName + '\'' +
               ", graylogHost='" + graylogHost + '\'' +
               ", graylogPort=" + graylogPort +
               ", connectTimeout=" + connectTimeout +
               ", reconnectDelay=" + reconnectDelay +
               ", tcpKeepAlive=" + tcpKeepAlive +
               ", tcpNoDelay=" + tcpNoDelay +
               ", queueSize=" + queueSize +
               ", maxInflightSends=" + maxInflightSends +
               ", useNowTimestamp=" + useNowTimestamp +
               ", messageFields='" + messageFields + '\'' +
               ", messageSummaryFields='" + messageSummaryFields + '\'' +
               ", contentType=" + contentType +
               '}';
    }
}
