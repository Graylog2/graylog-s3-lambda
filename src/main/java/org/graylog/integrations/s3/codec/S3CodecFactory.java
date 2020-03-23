package org.graylog.integrations.s3.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.s3.Configuration;

import javax.inject.Inject;
import javax.inject.Provider;


public class S3CodecFactory implements Provider<S3Codec> {
    private final Configuration config;
    private final ObjectMapper objectMapper;

    @Inject
    public S3CodecFactory(Configuration config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public S3Codec get() {
        switch (config.getContentType()) {
            case APPLICATION_JSON:
                return new ApplicationJsonCodec();
            case CLOUD_FLARE_LOG:
                return new CloudflareLogCodec(config, objectMapper);
            case TEXT_PLAIN:
                return new PlainTextCodec();
            default:
                throw new IllegalArgumentException(String.format("The content type [%s] is not yet supported. " +
                        "This is a development error.", config.getContentType()));
        }
    }
}
