package org.graylog.integrations.s3.codec;

import org.graylog2.gelfclient.GelfMessage;

public class PlainTextCodec implements S3Codec {

    public GelfMessage decode(String message) {

        // Nothing to do here, just pass the message along.
        return new GelfMessage(message);
    }
}
