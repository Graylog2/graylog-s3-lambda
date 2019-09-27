package org.graylog.integrations.s3;

class AbstractS3Codec {

    final String stringMessage;
    final Configuration config;

    AbstractS3Codec(String stringMessage, Configuration config) {
        this.stringMessage = stringMessage;
        this.config = config;
    }
}