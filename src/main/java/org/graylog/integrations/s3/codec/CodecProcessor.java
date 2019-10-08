package org.graylog.integrations.s3.codec;


import org.graylog.integrations.s3.Configuration;
import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;

public class CodecProcessor {

    private final Configuration config;
    private ApplicationJsonCodec applicationJsonCodec;
    private CloudFlareLogpushCodec cloudFlareLogpushCodec;
    private PlainTextCodec plainTextCodec;

    public CodecProcessor(Configuration config) {
        this.config = config;
        this.applicationJsonCodec = new ApplicationJsonCodec(config);
        this.cloudFlareLogpushCodec = new CloudFlareLogpushCodec(config);
        this.plainTextCodec = new PlainTextCodec(config);
    }

    /**
     * Choose the correct codec based on the {@code ContentType} and decode the message.
     *
     * @throws IOException
     */
    public GelfMessage decode(String message) throws IOException {

        switch (config.getContentType()) {
            case APPLICATION_JSON:
                return applicationJsonCodec.decode(message);
            case CLOUD_FLARE_LOGPUSH:
                return cloudFlareLogpushCodec.decode(message);
            case TEXT_PLAIN:
                return plainTextCodec.decode(message);
        }

        throw new IllegalArgumentException(String.format("The content type [%s] is not yet supported. " +
                                                         "This is a development error.", config.getContentType()));
    }
}
