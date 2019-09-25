package org.graylog.integrations.s3;

public class AbstractS3Codec {

    final String stringMessage;
    final Config config;

    public AbstractS3Codec(String stringMessage, Config config) {
        this.stringMessage = stringMessage;
        this.config = config;
    }
}