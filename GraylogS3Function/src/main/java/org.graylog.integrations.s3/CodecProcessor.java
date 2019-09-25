package org.graylog.integrations.s3;

import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;

public class CodecProcessor {

    private final String stringMessage;
    private final Config config;

    public CodecProcessor(Config config, String stringMessage) {
        this.stringMessage = stringMessage;
        this.config = config;
    }

    /**
     * Choose the correct codec based on the {@code ContentType} and decode the message.
     *
     * @throws IOException
     */
    public GelfMessage decode() throws IOException {

        switch (config.getContentType()) {
            case APPLICATION_JSON:
                return new ApplicationJsonCodec(stringMessage, config).decode();
            case CLOUD_FLARE_LOGPUSH:
                return new CloudFlareLogpushCodec(stringMessage, config).decode();
            case TEXT_PLAIN:
                return new PlainTextCodec(stringMessage, config).decode();
        }

        throw new IllegalArgumentException(String.format("The content type [%s] is not yet supported. " +
                                                         "This is a development error.", config.getContentType()));
    }
}
