package org.graylog.integrations.s3;

import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;

public class PlainTextCodec extends AbstractS3Codec implements S3Codec {

    public PlainTextCodec(String stringMessage, Config config) {
        super(stringMessage, config);
    }

    public GelfMessage decode() throws IOException {

        // Nothing to do here.
        return new GelfMessage(stringMessage);
    }
}
