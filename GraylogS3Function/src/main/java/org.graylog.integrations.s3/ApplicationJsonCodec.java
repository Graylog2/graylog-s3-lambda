package org.graylog.integrations.s3;

import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;

public class ApplicationJsonCodec extends AbstractS3Codec implements S3Codec {

    public ApplicationJsonCodec(String stringMessage, Config config) {
        super(stringMessage, config);
    }

    public GelfMessage decode() throws IOException {

        // TODO: Implement - Parse up some JSON.

        return new GelfMessage("");
    }
}
