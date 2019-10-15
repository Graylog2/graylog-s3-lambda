package org.graylog.integrations.s3.codec;

import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;

interface S3Codec {

    GelfMessage decode(String message) throws IOException;
}
