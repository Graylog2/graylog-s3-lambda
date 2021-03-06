package org.graylog.integrations.s3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog.integrations.s3.codec.S3CodecFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Configurable content type for the S3 Lambda function.
 *
 * Technically, MediaType or MimeType is a more accurate name for this configuration option (since ContentType is
 * specifically an HTTP construct). However, ContentType is used here, since it is the most clear/well know term.
 *
 * See {@link S3CodecFactory} for the log message codec selection.
 *
 * See {@link Configuration}.
 */
public enum ContentType {

    APPLICATION_JSON("application/json"),
    CLOUD_FLARE_LOG("application/x.cloudflare.log"), // "x." indicates a custom mime type.
    TEXT_PLAIN("text/plain");

    private static final Logger LOG = LogManager.getLogger(ContentType.class);
    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Get the ContentType enum for the specified type.
     *
     * Default to TEXT_PLAIN.
     */
    public static ContentType findByType(String type) {

        return Arrays.stream(ContentType.values())
                     .filter(Objects::nonNull)
                     .filter(v -> v.type.equals(type))
                     .findAny()
                     .orElseGet(() -> {
                         LOG.warn("ContentType [{}] not found. Defaulting to [{}].", type, TEXT_PLAIN);
                         return TEXT_PLAIN;
                     });
    }
}