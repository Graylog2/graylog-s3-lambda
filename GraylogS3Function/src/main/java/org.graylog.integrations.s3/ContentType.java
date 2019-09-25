package org.graylog.integrations.s3;

import java.util.Arrays;

public enum ContentType {
    APPLICATION_JSON("application/json"),
    CLOUD_FLARE_LOGPUSH("cloudflare/logpush"),
    TEXT_PLAIN("text/plain");

    private String type;

    ContentType(String type) {
        this.type = type;
    }

    /**
     * Get the ContentType enum for the specified type.
     */
    public static ContentType findByType(String type) {

        return Arrays.stream(ContentType.values())
                     .filter(v -> v.type.equals(type))
                     .findAny()
                     .orElseThrow(() -> new IllegalArgumentException(String.format("[%s] is not a valid content type.", type)));
    }
}
