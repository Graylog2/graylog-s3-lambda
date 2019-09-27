package org.graylog.integrations.s3;

/**
 * All CloudFlare Logpush-specific configuration.
 *
 * @see <a href="https://developers.cloudflare.com/logs/logpush/">CloudFlare Logpush</a>
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/tutorial-env_cli.html">S3 Environment Variables</a>
 */
class LogpushConfiguration extends AbstractConfiguration {

    private static final String DEFAULT_MESSAGE_SUMMARY_FIELDS = "ClientRequestHost,ClientRequestPath,OriginIP,ClientSrcPort,EdgeServerIP,EdgeResponseBytes";

    // Use newInstance() instead.
    private LogpushConfiguration() {
    }

    private static String LOG_PUSH_PREFIX = "CLOUDFLARE_LOGPUSH.";

    private static final String USE_NOW_TIMESTAMP = LOG_PUSH_PREFIX + "use_now_timestamp";
    private static final String MESSAGE_FIELDS = LOG_PUSH_PREFIX + "message_fields";
    // Fields to store in the message field in the GELF message field.
    private static final String MESSAGE_SUMMARY_FIELDS = LOG_PUSH_PREFIX + "message_summary_fields";

    // Overrides message timestamp with the current time.
    private Boolean useNowTimestamp;
    // Fields to parse and store with the message in Graylog
    private String messageFields;
    private String messageSummaryFields;

    static LogpushConfiguration newInstance() {
        final LogpushConfiguration config = new LogpushConfiguration();
        config.useNowTimestamp = readBoolean(USE_NOW_TIMESTAMP, false);
        // Defaults to all fields
        config.messageFields = getStringEnvironmentVariable(MESSAGE_FIELDS, null);
        // Defaults to the indicated fields
        config.messageSummaryFields = getStringEnvironmentVariable(MESSAGE_SUMMARY_FIELDS, DEFAULT_MESSAGE_SUMMARY_FIELDS);
        return config;
    }

    Boolean getUseNowTimestamp() {
        return useNowTimestamp;
    }

    void setUseNowTimestamp(Boolean useNowTimestamp) {
        this.useNowTimestamp = useNowTimestamp;
    }

    String getMessageFields() {
        return messageFields;
    }

    void setMessageFields(String messageFields) {
        this.messageFields = messageFields;
    }

    String getMessageSummaryFields() {
        return messageSummaryFields;
    }

    void setMessageSummaryFields(String messageSummaryFields) {
        this.messageSummaryFields = messageSummaryFields;
    }

    @Override
    public String toString() {
        return "LogpushConfiguration{" +
               "useNowTimestamp=" + useNowTimestamp +
               ", messageFields='" + messageFields + '\'' +
               ", messageSummaryFields='" + messageSummaryFields + '\'' +
               '}';
    }
}
