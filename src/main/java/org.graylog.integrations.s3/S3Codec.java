package org.graylog.integrations.s3;

import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;

public interface S3Codec {

    GelfMessage decode() throws IOException;
}
