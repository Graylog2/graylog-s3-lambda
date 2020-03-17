package org.graylog.integrations.s3.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.s3.Configuration;


public class S3CodecFactory {
    private final Configuration config;
    private final ObjectMapper objectMapper;


    public S3CodecFactory(Configuration config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public S3Codec getCodec() {
        switch (config.getContentType()) {
            case APPLICATION_JSON:
                return new ApplicationJsonCodec(config);
            case CLOUD_FLARE_LOG:
                return new CloudflareLogCodec(config, objectMapper);
            case TEXT_PLAIN:
                return new PlainTextCodec(config);
            default:
                throw new IllegalArgumentException(String.format("The content type [%s] is not yet supported. " +
                        "This is a development error.", config.getContentType()));
        }
    }
}
