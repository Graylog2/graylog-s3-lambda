package org.graylog.integrations.s3;

public class AbstractS3Codec {

    final String stringMessage;
    final Configuration config;

    public AbstractS3Codec(String stringMessage, Configuration config) {
        this.stringMessage = stringMessage;
        this.config = config;
    }
}