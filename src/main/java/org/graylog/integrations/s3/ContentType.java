package org.graylog.integrations.s3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Configurable content type for the S3 Lambda function.
 * See {@link org.graylog.integrations.s3.config.Configuration}.
 */
public enum ContentType {

    APPLICATION_JSON("application/json"),
    CLOUD_FLARE_LOGPUSH("cloudflare/logpush"),
    TEXT_PLAIN("text/plain");

    private static final Logger LOG = LogManager.getLogger(ContentType.class);
    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    /**
     * Get the ContentType enum for the specified type.
     *
     * Default to TEXT_PLAIN.
     */
    public static ContentType findByType(String type) {

        return Arrays.stream(ContentType.values())
                     .filter(v -> v.type.equals(type))
                     .findAny()
                     .orElseGet(() -> {
                         LOG.warn("ContentType [{}] not found. Defaulting to [{}].", type, TEXT_PLAIN);
                         return TEXT_PLAIN;
                     });
    }
}