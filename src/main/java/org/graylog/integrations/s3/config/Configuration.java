package org.graylog.integrations.s3.config;

import org.graylog.integrations.s3.CompressionType;
import org.graylog.integrations.s3.ContentType;
import org.graylog.integrations.s3.ProtocolType;
import org.graylog2.gelfclient.Compression;

/**
 * This class reads the needed configuration values from environment variables defined on the S3 function.
 *
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/tutorial-env_cli.html">S3 Environment Variables</a>
 */
public class Configuration extends AbstractConfiguration {

    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_RECONNECT_DELAY = 10000;
    private static final int DEFAULT_TCP_QUEUE_SIZE = 512;
    private static final int DEFAULT_TCP_MAX_IN_FLIGHT_SENDS = 512;
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    // Use newInstance() instead.
    private Configuration() {
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
    private static final String CONTENT_TYPE = "content_type";
    private static final String COMPRESSION_TYPE = "compression_type";
    private static final String PROTOCOL_TYPE = "protocol_type";

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
    private ContentType contentType;
    private CompressionType compressionType;
    private ProtocolType protocolType;

    private LogpushConfiguration logPushConfiguration;

    public static Configuration newInstance() {
        final Configuration config = new Configuration();
        config.s3BucketName = System.getenv(S3_BUCKET_NAME);
        config.graylogHost = System.getenv(GRAYLOG_HOST);
        config.graylogPort = safeParseInteger(GRAYLOG_PORT);
        config.connectTimeout = safeParseInteger(CONNECT_TIMEOUT) != null ? safeParseInteger(CONNECT_TIMEOUT) : DEFAULT_CONNECT_TIMEOUT;
        config.reconnectDelay = safeParseInteger(RECONNECT_DELAY) != null ? safeParseInteger(RECONNECT_DELAY) : DEFAULT_RECONNECT_DELAY;
        config.tcpKeepAlive = readBoolean(TCP_KEEP_ALIVE, true);
        config.tcpNoDelay = readBoolean(TCP_NO_DELAY, true);
        config.queueSize = safeParseInteger(TCP_QUEUE_SIZE) != null ? safeParseInteger(TCP_QUEUE_SIZE) : DEFAULT_TCP_QUEUE_SIZE;
        config.maxInflightSends = safeParseInteger(TCP_MAX_IN_FLIGHT_SENDS) != null ? safeParseInteger(TCP_MAX_IN_FLIGHT_SENDS) : DEFAULT_TCP_MAX_IN_FLIGHT_SENDS;
        config.contentType = ContentType.findByType(getStringEnvironmentVariable(CONTENT_TYPE, null));
        config.compressionType = CompressionType.findByType(getStringEnvironmentVariable(COMPRESSION_TYPE, null));
        config.protocolType = ProtocolType.findByType(getStringEnvironmentVariable(PROTOCOL_TYPE, null));
        config.logPushConfiguration = LogpushConfiguration.newInstance();
        return config;
    }

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
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public CompressionType getCompressionType() {
        return compressionType;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public LogpushConfiguration getLogpushConfiguration() {
        return logPushConfiguration;
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
               ", contentType=" + contentType +
               ", compressionType=" + compressionType +
               ", logpushConfiguration=" + logPushConfiguration +
               '}';
    }
}
