package org.graylog.integrations.s3.codec;


import org.graylog.integrations.s3.config.Configuration;
import org.graylog2.gelfclient.GelfMessage;

public class PlainTextCodec extends AbstractS3Codec implements S3Codec {

    PlainTextCodec(Configuration config) {
        super(config);
    }

    public GelfMessage decode(String message) {

        // Nothing to do here, just pass the message along.
        return new GelfMessage(message);
    }
}
