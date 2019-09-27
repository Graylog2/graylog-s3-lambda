package org.graylog.integrations.s3.codec;


import org.graylog.integrations.s3.config.Configuration;
import org.graylog2.gelfclient.GelfMessage;

public class PlainTextCodec extends AbstractS3Codec implements S3Codec {

    PlainTextCodec(String stringMessage, Configuration config) {
        super(stringMessage, config);
    }

    public GelfMessage decode() {

        // Nothing to do here, just pass the message along.
        return new GelfMessage(stringMessage);
    }
}
