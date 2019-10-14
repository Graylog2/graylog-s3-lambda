package org.graylog.integrations.s3;

import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.converters.TrimmedStringListConverter;
import com.github.joschi.jadconfig.validators.PositiveIntegerValidator;
import com.github.joschi.jadconfig.validators.StringNotBlankValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads configuration values from environment variables defined on the Lambda function.
 *
 * Defaults are specified in the getter methods.
 *
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/tutorial-env_cli.html">Lambda Environment Variables</a>
 */
public class Configuration {

    // Environment variables with these names can be defined on the Lambda function to specify values.
    private static final String LOG_LEVEL = "LOG_LEVEL";
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

    // Allows user-defined log level. Null by default to avoid setting logger level in default scenario.
    @Parameter(value = LOG_LEVEL)
    private String logLevel;

    @Parameter(value = GRAYLOG_HOST, required = true, validators = StringNotBlankValidator.class)
    private String graylogHost;

    @Parameter(value = GRAYLOG_PORT, required = true, validators = PositiveIntegerValidator.class)
    private int graylogPort = 12201;

    @Parameter(value = CONNECT_TIMEOUT, required = true, validators = PositiveIntegerValidator.class)
    private int connectTimeout = 10000;

    @Parameter(value = RECONNECT_DELAY, required = true, validators = PositiveIntegerValidator.class)
    private int reconnectDelay = 10000;

    @Parameter(value = TCP_KEEP_ALIVE, required = true)
    private boolean tcpKeepAlive = true;

    @Parameter(value = TCP_NO_DELAY, required = true)
    private boolean tcpNoDelay = true;

    @Parameter(value = TCP_QUEUE_SIZE, required = true, validators = PositiveIntegerValidator.class)
    private int queueSize = 512;

    @Parameter(value = TCP_MAX_IN_FLIGHT_SENDS, required = true, validators = PositiveIntegerValidator.class)
    private int maxInflightSends = 512;

    @Parameter(value = CONTENT_TYPE, required = true, validators = StringNotBlankValidator.class)
    private String contentType = ContentType.TEXT_PLAIN.toString();

    @Parameter(value = COMPRESSION_TYPE, required = true, validators = StringNotBlankValidator.class)
    private String compressionType = CompressionType.NONE.toString();

    @Parameter(value = PROTOCOL_TYPE, required = true, validators = StringNotBlankValidator.class)
    private String protocolType = ProtocolType.TCP.toString();

    // The number of milliseconds to wait for messages to finish sending during shutdown.
    @Parameter(value = SHUTDOWN_FLUSH_TIMEOUT_MS, required = true, validators = PositiveIntegerValidator.class)
    private int shutdownFlushTimeoutMs = 100;

    // How many times to retry the shutdownFlushTimeoutMs wait while waiting for messages to
    // finish sending during shutdown.
    @Parameter(value = SHUTDOWN_FLUSH_RETRIES, required = true, validators = PositiveIntegerValidator.class)
    private int shutdownFlushReties = 600;

    // ** Logpush specific fields.

    // Overrides message timestamp with the current time.
    @Parameter(value = LOGPUSH_USE_NOW_TIMESTAMP, required = true)
    private boolean useNowTimestamp = false;

    // Fields to parse and store with the message in Graylog. This defaults to all.
    @Parameter(value = LOGPUSH_MESSAGE_FIELDS, converter = TrimmedStringListConverter.class)
    private List<String> messageFields = new ArrayList<>();

    @Parameter(value = LOGPUSH_MESSAGE_SUMMARY_FIELDS, required = true, converter = TrimmedStringListConverter.class)
    private List<String> messageSummaryFields = Arrays.asList("ClientRequestHost", "ClientRequestPath", "OriginIP", "ClientSrcPort", "EdgeServerIP", "EdgeResponseBytes");

    public String getLogLevel() {
        return logLevel;
    }

    public String getGraylogHost() {
        return graylogHost;
    }

    public int getGraylogPort() {
        return graylogPort;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }

    public boolean getTcpKeepAlive() {
        return tcpKeepAlive;
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getMaxInflightSends() {
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

    public int getShutdownFlushTimeoutMs() {
        return shutdownFlushTimeoutMs;
    }

    public int getShutdownFlushReties() {
        return shutdownFlushReties;
    }

    public boolean getUseNowTimestamp() {
        return useNowTimestamp;
    }

    public List<String> getMessageFields() {
        return messageFields;
    }

    public void setMessageFields(List<String> messageFields) {
        this.messageFields = messageFields;
    }

    public List<String> getMessageSummaryFields() {
        return messageSummaryFields;
    }

    public void setMessageSummaryFields(List<String> messageSummaryFields) {
        this.messageSummaryFields = messageSummaryFields;
    }

    @Override
    public String toString() {
        return "Configuration{" +
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
