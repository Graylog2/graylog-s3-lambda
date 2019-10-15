package org.graylog.integrations.s3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;

/**
 * Configurable compression type for the S3 Lambda function.
 * See {@link Configuration}.
 */
public enum CompressionType {
    GZIP("gzip"),
    NONE("none");

    private static final Logger LOG = LogManager.getLogger(CompressionType.class);
    private final String type;

    CompressionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Get the CompressionType enum for the specified type.
     * Default to NONE.
     */
    public static CompressionType findByType(String type) {

        return Arrays.stream(CompressionType.values())
                     .filter(Objects::nonNull)
                     .filter(v -> v.type.equals(type))
                     .findAny()
                     .orElseGet(() -> {
                         LOG.warn("Compression type [{}] not found. Defaulting to [{}].", type, NONE);
                         return NONE;
                     });
    }
}
