package org.graylog.integrations.s3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog2.gelfclient.GelfTransports;

import java.util.Arrays;
import java.util.Objects;

/**
 * Configurable protocol type for the S3 Lambda function.
 * See {@link Configuration}.
 */
public enum ProtocolType {
    TCP("tcp", GelfTransports.TCP),
    UDP("udp", GelfTransports.UDP);

    private static final Logger LOG = LogManager.getLogger(ProtocolType.class);
    private final String type;
    private final GelfTransports gelfTransport;

    ProtocolType(String type, GelfTransports gelfTransport) {
        this.type = type;
        this.gelfTransport = gelfTransport;
    }

    public GelfTransports getGelfTransport() {
        return gelfTransport;
    }

    /**
     * Get the ProtocolType enum for the specified type.
     * Default to TCP.
     */
    public static ProtocolType findByType(String type) {

        return Arrays.stream(ProtocolType.values())
                     .filter(Objects::nonNull)
                     .filter(v -> v.type.equals(type))
                     .findAny()
                     .orElseGet(() -> {
                         LOG.warn("Protocol type [{}] not found. Defaulting to [{}].", type, TCP);
                         return TCP;
                     });
    }
}
